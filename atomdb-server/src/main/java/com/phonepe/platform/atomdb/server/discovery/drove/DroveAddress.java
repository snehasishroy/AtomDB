package com.phonepe.platform.atomdb.server.discovery.drove;

import com.phonepe.platform.atomdb.server.discovery.Address;


public class DroveAddress extends Address {

    long createdAt;

    public DroveAddress(String host,
                        int port,
                        long createdAt) {
        super(host, port);
        this.createdAt = createdAt;
    }
}
