package com.phonepe.platform.atomdb.server.discovery.drove;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.FailsafeException;
import net.jodah.failsafe.RetryPolicy;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

@Slf4j
public class NativeHTTPDroveClient implements DroveClient {

    private static final String API = "/apis/v1/internal/instances";
    private final String authToken;
    private final List<String> endpoints;
    private final String portName;

    private final AtomicReference<String> leader = new AtomicReference<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public NativeHTTPDroveClient(final String authToken,
                                 final List<String> endpoints,
                                 final String portName) {
        this.authToken = authToken;
        this.endpoints = endpoints;
        this.portName = portName;
        objectMapper.registerModule(new ParameterNamesModule());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
    }

    @Override
    public List<DroveAddress> peers() {
        // retries all the endpoints one by one in case the result is empty
        RetryPolicy<List<DroveAddress>> policy = new RetryPolicy<List<DroveAddress>>().withDelay(Duration.ofSeconds(1))
                .withMaxAttempts(endpoints.size())
                .handle(Exception.class)
                .handleResultIf(List::isEmpty);
        try {
            return Failsafe.with(policy)
                    .get(this::readData);
        } catch (FailsafeException e) {
            log.error("Error getting instance information: ", e);
        }
        return Collections.emptyList();
    }

    private List<DroveAddress> readData() {
        String currLeader = leader.get();
        List<DroveAddress> result = Collections.emptyList();
        if (null != currLeader) {
            log.info("Calling leader: {}", currLeader);
            try {
                result = readFromEndpoint(currLeader);
            } catch (IOException e) {
                log.error("Error reading endpoints from current leader {}", currLeader, e);
            }
        }
        if (!result.isEmpty()) {
            return result;
        }
        return endpoints.stream()
                .map(endpoint -> {
                    try {
                        return readFromEndpoint(endpoint);
                    } catch (IOException e) {
                        log.error("Error reading instance list from {}", endpoint, e);
                    }
                    return Collections.<DroveAddress>emptyList();
                })
                .filter(nodes -> !nodes.isEmpty())
                .findAny()
                .orElse(Collections.emptyList());
    }

    private List<DroveAddress> readFromEndpoint(final String currEndPoint) throws IOException {
        URL obj = new URL(currEndPoint + API);
        log.info("Drove call url: {}", obj);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("App-Instance-Authorization", authToken);
        int responseCode = con.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            leader.set(currEndPoint);
            log.info("Leader set to: {}", currEndPoint);
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            JsonNode response = objectMapper.readTree(in);
            log.info("Drove response: {}", response);

            if (response.get("status")
                    .asText()
                    .equals("SUCCESS")) {
                JsonNode instanceList = response.get("data");
                if (instanceList.isNull() || instanceList.isMissingNode()) {
                    return Collections.emptyList();
                }
                ArrayList<DroveAddress> addresses = new ArrayList<>();
                (instanceList).forEach(node -> {
                    JsonNode hostNode = node.at("/localInfo/hostname");
                    JsonNode portNode = node.at("/localInfo/ports/" + portName + "/hostPort");
                    JsonNode createdAtNode = node.at("/created");
                    JsonNode instanceIdNode = node.at("/instanceId");
                    if (!hostNode.isMissingNode() && !portNode.isMissingNode() && !createdAtNode.isMissingNode()
                            && !instanceIdNode.isMissingNode()) {
                        String host = hostNode.asText();
                        int port = portNode.asInt();
                        long createdAt = createdAtNode.asLong();
                        String instanceId = instanceIdNode.asText();
                        if (StringUtils.isNotEmpty(host) && port != 0 && createdAt != 0 && StringUtils.isNotEmpty(
                                instanceId)) {
                            log.info("Found node: {}:{}:{}:{}", host, port, createdAt, instanceId);
                            addresses.add(new DroveAddress(host, port, createdAt, instanceId));
                        } else {
                            log.warn("Invalid address info: host {} port {} createdAt {}", host, port, createdAt);
                        }
                    } else {
                        log.error("Missing info in instance node: {}", node);
                    }
                });
                return addresses;
            }
        } else {
            log.error("Error getting instance list. Status: {} {}: {}", con.getResponseCode(), con.getResponseMessage(),
                    IOUtils.toString(con.getErrorStream()));
        }
        return Collections.emptyList();
    }
}