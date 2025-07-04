package com.phonepe.platform.atomdb.server.discovery;

import java.util.Map;
import lombok.Value;

@Value
public class SimpleDiscoveryNode extends DiscoveryNode {

    Address publicAddress;
    Map<String, Object> properties;

}