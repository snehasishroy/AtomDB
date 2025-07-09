package com.phonepe.platform.atomdb.server;

import com.phonepe.platform.atomdb.server.discovery.ClusterHealthStrategy;
import com.phonepe.platform.atomdb.server.discovery.ClusterManager;
import com.phonepe.platform.atomdb.server.discovery.ClusterService;
import com.phonepe.platform.atomdb.server.discovery.ClusterServiceImpl;
import com.phonepe.platform.atomdb.server.discovery.DiscoveryStrategy;
import com.phonepe.platform.atomdb.server.discovery.FixedSizeClusterStrategy;
import com.phonepe.platform.atomdb.server.discovery.simple.SimpleEndpointDiscoveryStrategy;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DummyTest {

    public static void main(String[] args) {
        System.setProperty(SimpleEndpointDiscoveryStrategy.PEERS_PROPERTY,
                "n1:0.0.0.0:8090,n2:0.0.0.0:8091,n3:0.0.0.0:8092");
        System.setProperty(ClusterManager.INSTANCE_ID, "n3");
        new DummyTest().run();
    }

    private void run() {
        DiscoveryStrategy discoveryStrategy = new SimpleEndpointDiscoveryStrategy();
        ClusterHealthStrategy clusterHealthStrategy = new FixedSizeClusterStrategy(3);
        ClusterService clusterService = new ClusterServiceImpl(discoveryStrategy, clusterHealthStrategy);
        ClusterManager clusterManager = new ClusterManager(clusterService);
        try {
            clusterManager.manage();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Runtime.getRuntime()
                .addShutdownHook(new Thread(() -> {
                    log.info("Server shutdown complete");
                }));
    }
}
