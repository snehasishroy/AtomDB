package com.phonepe.platform.atomdb.server.discovery;

import java.util.List;

public interface ClusterHealthStrategy {

    boolean isHealthy(List<DiscoveryNode> nodes);
}
