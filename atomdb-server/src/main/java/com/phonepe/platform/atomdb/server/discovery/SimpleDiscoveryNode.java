package com.phonepe.platform.atomdb.server.discovery;

import java.util.Map;
import lombok.Value;

@Value
public class SimpleDiscoveryNode extends DiscoveryNode {

    Address publicAddress;
    String nodeId;
    Map<String, Object> properties;

}