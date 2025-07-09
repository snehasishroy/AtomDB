package com.phonepe.platform.atomdb.server.discovery.simple;

import com.phonepe.platform.atomdb.server.discovery.Address;
import com.phonepe.platform.atomdb.server.discovery.ClusterManager;
import com.phonepe.platform.atomdb.server.discovery.DiscoveryNode;
import com.phonepe.platform.atomdb.server.discovery.DiscoveryStrategy;
import com.phonepe.platform.atomdb.server.discovery.SimpleDiscoveryNode;
import com.phonepe.platform.atomdb.server.utils.ConfigUtils;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SimpleEndpointDiscoveryStrategy implements DiscoveryStrategy {

    public static final String PEERS_ENV_VARIABLE_NAME = "PEERS";
    public static final String PEERS_PROPERTY = "peers.list";

    List<Address> peerAddresses;

    public SimpleEndpointDiscoveryStrategy() {
        start();
    }

    @Override
    public void start() {
        // no-op
    }

    @Override
    public List<DiscoveryNode> discoverNodes() {
        String peers = ConfigUtils.readEnvOrProperty(PEERS_ENV_VARIABLE_NAME, PEERS_PROPERTY);
        String[] split = peers.split(",");
        List<String> peerList = List.of(split);
        return peerList.stream()
                .map(peer -> {
                    // TODO: Add validations
                    // format: nodeId:hostname:port
                    String[] peerInfo = peer.split(":");
                    System.setProperty(ClusterManager.INSTANCE_ID, peerInfo[0]);
                    Address address = new Address(peerInfo[1], Integer.parseInt(peerInfo[2]));
                    return new SimpleDiscoveryNode(address, peerInfo[0], Map.of());
                })
                .collect(Collectors.toList());
    }

    @Override
    public void stop() {
        // no-op
    }
}
