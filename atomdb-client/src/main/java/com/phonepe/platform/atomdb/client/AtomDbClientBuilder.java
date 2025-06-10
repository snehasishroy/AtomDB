package com.phonepe.platform.atomdb.client;

import com.google.common.base.Preconditions;
import com.phonepe.platform.atomdb.client.impl.AtomDbHttpClient;
import com.phonepe.platform.http.v2.client.ClientFactory;
import com.phonepe.platform.http.v2.discovery.ServiceEndpointProviderFactory;
import com.phonepe.platform.http.v2.executor.factory.HttpExecutorBuilderFactory;
import io.dropwizard.setup.Environment;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import javax.validation.constraints.NotNull;

@UtilityClass
public class AtomDbClientBuilder {
    @SneakyThrows
    public static AtomDbClient build(
            @NotNull final AtomDbClientConfig config,
            final ServiceEndpointProviderFactory endpointProviderFactory,
            final Environment environment
    ) {
        Preconditions.checkArgument(config != null, "config cannot be null");

        final var client = ClientFactory.newHttpClientBuilder()
                .withMetricRegistry(environment.metrics())
                .withConfiguration(config.getHttpConfiguration())
                .build();

        final var executorBuilderFactory = HttpExecutorBuilderFactory.builder()
                .client(client)
                .endpointProvider(endpointProviderFactory.provider(config.getHttpConfiguration(), environment))
                .mapper(environment.getObjectMapper())
                .callerClass(AtomDbClient.class)
                .build();

        return new AtomDbHttpClient(executorBuilderFactory);
    }
}
