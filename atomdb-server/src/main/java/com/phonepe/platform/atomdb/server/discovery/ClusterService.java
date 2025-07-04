package com.phonepe.platform.atomdb.server.discovery;

import java.util.List;

public interface ClusterService {

    List<DiscoveryNode> awaitDiscovery();
}
