package com.phonepe.platform.atomdb.client.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.phonepe.platform.atomdb.client.AtomDbClient;
import com.phonepe.platform.atomdb.models.AtomDbResponse;
import com.phonepe.platform.http.v2.executor.factory.HttpExecutorBuilderFactory;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import javax.annotation.Nullable;
import javax.ws.rs.core.UriBuilder;

@AllArgsConstructor
public class AtomDbHttpClient implements AtomDbClient {
    private final HttpExecutorBuilderFactory executorFactory;

    @Override
    @SneakyThrows
    public AtomDbResponse<String> ping(final String prefix, @Nullable final String message) {
        final var builder = UriBuilder.fromPath("/housekeeping/v1/ping/"+ prefix);
        if (message != null) {
            builder.queryParam("message", message);
        }

        return executorFactory.<AtomDbResponse<String>>httpGetExecutorBuilder()
                .responseTypeReference(new TypeReference<>() {})
                .url(builder.build().toString())
                .command("ping")
                .build()
                .executeTracked();
    }
}
