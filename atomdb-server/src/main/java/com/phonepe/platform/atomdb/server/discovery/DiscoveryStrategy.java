package com.phonepe.platform.atomdb.server.discovery;

import java.util.List;

public interface DiscoveryStrategy {

    void start();

    List<DiscoveryNode> discoverNodes();

    void stop();
}
