package com.phonepe.platform.atomdb.server.discovery;

import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FixedSizeClusterStrategy implements ClusterHealthStrategy {

    private final int minSize;

    FixedSizeClusterStrategy(int minSize) {
        this.minSize = minSize;
    }

    @Override
    public boolean isHealthy(List<DiscoveryNode> nodes) {
        log.info("Discovered {} nodes, Required {}", nodes.size(), minSize);
        return nodes.size() >= minSize;
    }
}
