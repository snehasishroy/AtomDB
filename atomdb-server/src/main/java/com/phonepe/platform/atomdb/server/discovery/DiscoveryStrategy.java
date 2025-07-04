package com.phonepe.platform.atomdb.server.discovery;

public interface DiscoveryStrategy {

    void start();

    Iterable<DiscoveryNode> discoverNodes();

    void stop();
}
