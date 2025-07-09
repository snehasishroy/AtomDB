package com.phonepe.platform.atomdb.server.discovery.drove;

import com.phonepe.platform.atomdb.server.discovery.Address;
import lombok.Getter;


@Getter
public class DroveAddress extends Address {

    private final long createdAt;
    private final String instanceId;

    public DroveAddress(String host,
                        int port,
                        long createdAt,
                        String instanceId) {
        super(host, port);
        this.createdAt = createdAt;
        this.instanceId = instanceId;
    }
}
