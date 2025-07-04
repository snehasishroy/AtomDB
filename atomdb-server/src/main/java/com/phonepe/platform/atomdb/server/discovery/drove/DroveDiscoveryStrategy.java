package com.phonepe.platform.atomdb.server.discovery.drove;

import com.phonepe.platform.atomdb.server.discovery.DiscoveryNode;
import com.phonepe.platform.atomdb.server.discovery.DiscoveryStrategy;
import com.phonepe.platform.atomdb.server.discovery.SimpleDiscoveryNode;
import com.phonepe.platform.atomdb.server.utils.ConfigUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class DroveDiscoveryStrategy implements DiscoveryStrategy {

    private static final String SPLIT_DELIMITER = ",";

    // token key to call drove controller
    private static final String AUTH_TOKEN_ENV_VARIABLE_NAME = "DROVE_APP_INSTANCE_AUTH_TOKEN";
    public static final String AUTH_TOKEN_PROPERTY = "discovery.drove.token";

    // drove endpoints (comma separated)
    private static final String DROVE_ENDPOINTS_ENV_VARIABLE_NAME = "DISCOVERY_DROVE";
    private static final String DROVE_ENDPOINTS_PROPERTY = "discovery.drove";

    // raft port
    private static final String RAFT_PORT_NAME_ENV_VARIABLE_NAME = "RAFT_PORT";
    private static final String RAFT_PORT_NAME_PROPERTY = "raft.port";
    public static final String CREATED_AT = "createdAt";

    private DroveClient client;

    @SneakyThrows
    public DroveDiscoveryStrategy(Map<String, Comparable> properties) {
        start();
    }

    @Override
    public void start() {
        log.info("Starting Drove Strategy");
        String droveEndpoints = readDroveEndpoints();
        log.info("Drove endpoint received as {}", droveEndpoints);
        Objects.requireNonNull(droveEndpoints, "Drove endpoint can't be empty");

        String authToken = readToken();
        log.debug("Auth token received as : {}", authToken);
        Objects.requireNonNull(authToken, "Drove authToken cannot be empty!!!");

        String portName = readRaftPort();
        log.info("Raft port received as : {}", portName);
        Objects.requireNonNull(portName, "Raft port cannot be empty!!!");

        client = new NativeHTTPDroveClient(authToken, endpoints(droveEndpoints), portName);
    }

    @Override
    public List<DiscoveryNode> discoverNodes() {
        List<DroveAddress> peers = client.peers();
        if (!peers.isEmpty()) {
            log.info("Peer list: {}", peers.stream()
                    .map(address -> address.getHost() + ":" + address.getPort() + ":" + address.getCreatedAt())
                    .toList());
        } else {
            log.warn("Could not fetch peer list.");
        }
        return peers.stream()
                .map(address -> {
                    Map<String, Object> properties = Map.of(CREATED_AT, address.getCreatedAt());

                    return new SimpleDiscoveryNode(address, properties);
                })
                .sorted((o1, o2) -> {
                    // sort by created at for a fixed ordering
                    Long o1CreatedAt = (Long) o1.getProperties()
                            .get(CREATED_AT);
                    return o1CreatedAt.compareTo((Long) o2.getProperties()
                            .get(CREATED_AT));
                })
                .collect(Collectors.toList());
    }

    @Override
    public void stop() {
        // no-op
    }

    private List<String> endpoints(final String droveEndpoint) {
        val endpoints = droveEndpoint.split(SPLIT_DELIMITER);
        if (endpoints.length == 0) {
            throw new RuntimeException("No drove endpoint found for hazelcast discovery!");
        }
        return Arrays.asList(endpoints);

    }

    private String readToken() {
        return ConfigUtils.readEnvOrProperty(AUTH_TOKEN_ENV_VARIABLE_NAME, AUTH_TOKEN_PROPERTY);
    }

    private String readDroveEndpoints() {
        return ConfigUtils.readEnvOrProperty(DROVE_ENDPOINTS_ENV_VARIABLE_NAME, DROVE_ENDPOINTS_PROPERTY);
    }

    private String readRaftPort() {
        return ConfigUtils.readEnvOrProperty(RAFT_PORT_NAME_ENV_VARIABLE_NAME, RAFT_PORT_NAME_PROPERTY);
    }
}