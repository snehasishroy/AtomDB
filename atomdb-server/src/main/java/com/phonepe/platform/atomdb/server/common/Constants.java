package com.phonepe.platform.atomdb.server.common;

import com.google.common.base.Strings;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

    public final String HOSTNAME = hostname();

    private String hostname() {
        final String host = System.getenv("HOST");
        return Strings.isNullOrEmpty(host) ? "UNKNOWN" : host;
    }
}
