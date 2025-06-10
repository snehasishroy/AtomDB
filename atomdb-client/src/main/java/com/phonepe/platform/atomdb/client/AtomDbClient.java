package com.phonepe.platform.atomdb.client;

import com.phonepe.platform.atomdb.models.AtomDbResponse;

import javax.annotation.Nullable;

public interface AtomDbClient {
    default AtomDbResponse<String> ping(final String prefix) {
        return ping(prefix, null);
    }

    AtomDbResponse<String> ping(final String prefix, final @Nullable String message);
}
