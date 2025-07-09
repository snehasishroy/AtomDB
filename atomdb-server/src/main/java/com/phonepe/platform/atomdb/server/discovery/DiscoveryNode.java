package com.phonepe.platform.atomdb.server.discovery;

import java.util.Map;

public abstract class DiscoveryNode {

    public abstract String getNodeId();

    public abstract Address getPublicAddress();

    public abstract Map<String, Object> getProperties();
}