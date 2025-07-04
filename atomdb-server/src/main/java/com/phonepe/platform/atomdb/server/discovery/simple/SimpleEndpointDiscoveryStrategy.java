package com.phonepe.platform.atomdb.server.discovery.simple;

import com.phonepe.platform.atomdb.server.discovery.Address;
import com.phonepe.platform.atomdb.server.discovery.DiscoveryNode;
import com.phonepe.platform.atomdb.server.discovery.DiscoveryStrategy;
import com.phonepe.platform.atomdb.server.discovery.SimpleDiscoveryNode;
import com.phonepe.platform.atomdb.server.utils.ConfigUtils;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SimpleEndpointDiscoveryStrategy implements DiscoveryStrategy {

    private static final String PEERS_ENV_VARIABLE_NAME = "PEERS";
    private static final String PEERS_PROPERTY = "peers.list";

    List<Address> peerAddresses;

    public SimpleEndpointDiscoveryStrategy() {
        start();
    }

    @Override
    public void start() {
        String peers = ConfigUtils.readEnvOrProperty(PEERS_ENV_VARIABLE_NAME, PEERS_PROPERTY);
        String[] split = peers.split(",");
        List<String> peerList = List.of(split);
        peerAddresses = peerList.stream()
                .map(peer -> {
                    // TODO: Add validations
                    String[] hostAndPort = peer.split(":");
                    return new Address(hostAndPort[0], Integer.parseInt(hostAndPort[1]));
                })
                .toList();
    }

    @Override
    public List<DiscoveryNode> discoverNodes() {
        return peerAddresses.stream()
                .map(address -> new SimpleDiscoveryNode(address, Map.of()))
                .collect(Collectors.toList());
    }

    @Override
    public void stop() {
        // no-op
    }
}
