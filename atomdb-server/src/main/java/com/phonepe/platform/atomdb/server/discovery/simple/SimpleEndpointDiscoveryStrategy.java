package com.phonepe.platform.atomdb.server.discovery.simple;

import com.phonepe.platform.atomdb.server.discovery.DiscoveryNode;
import com.phonepe.platform.atomdb.server.discovery.DiscoveryStrategy;
import com.phonepe.platform.atomdb.server.utils.ConfigUtils;

public class SimpleEndpointDiscoveryStrategy implements DiscoveryStrategy {

    private static final String PEERS_ENV_VARIABLE_NAME = "PEERS";
    private static final String PEERS_PROPERTY = "peers.list";

    public SimpleEndpointDiscoveryStrategy() {
        start();
    }

    @Override
    public void start() {
        String peers = ConfigUtils.readEnvOrProperty(PEERS_ENV_VARIABLE_NAME, PEERS_PROPERTY);
        String[] split = peers.split(",");

    }

    @Override
    public Iterable<DiscoveryNode> discoverNodes() {
        return null;
    }

    @Override
    public void stop() {
        // no-op
    }
}
