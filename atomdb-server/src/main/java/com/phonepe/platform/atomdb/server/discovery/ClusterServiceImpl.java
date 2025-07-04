package com.phonepe.platform.atomdb.server.discovery;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClusterServiceImpl implements ClusterService {

    private final DiscoveryStrategy discoveryStrategy;
    private final ClusterHealthStrategy clusterHealthStrategy;

    public ClusterServiceImpl(DiscoveryStrategy discoveryStrategy,
                              ClusterHealthStrategy clusterHealthStrategy) {
        this.discoveryStrategy = discoveryStrategy;
        this.clusterHealthStrategy = clusterHealthStrategy;
    }

    @Override
    public List<DiscoveryNode> awaitDiscovery() {
        while (true) {
            List<DiscoveryNode> nodes = discoveryStrategy.discoverNodes();
            if (clusterHealthStrategy.isHealthy(nodes)) {
                return nodes;
            } else {
                LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1));
                log.info("Sleeping for 1 second before retrying discovery");
            }
        }
    }
}
