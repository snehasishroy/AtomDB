package com.phonepe.platform.atomdb.server.discovery;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class Address {

    String host;
    int port;
}