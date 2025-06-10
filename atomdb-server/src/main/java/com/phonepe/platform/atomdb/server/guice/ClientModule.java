package com.phonepe.platform.atomdb.server.guice;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.phonepe.dataplatform.EventIngestorClient;
import com.phonepe.dataplatform.EventIngestorClientConfig;
import com.phonepe.olympus.im.client.OlympusIMClient;
import com.phonepe.platform.http.v2.client.ClientFactory;
import com.phonepe.platform.http.v2.common.HttpConfiguration;
import com.phonepe.platform.http.v2.discovery.ServiceEndpointProviderFactory;
import com.phonepe.platform.http.v2.executor.factory.HttpExecutorBuilderFactory;
import com.phonepe.platform.atomdb.server.exception.AtomDbErrorCode;
import com.phonepe.platform.atomdb.server.exception.AtomDbError;
import com.phonepe.platform.atomdb.server.guice.bindings.BindingNames;
import com.phonepe.platform.atomdb.server.guice.bindings.OlympusAuthSupplier;
import com.phonepe.platform.atomdb.server.services.impl.SampleServiceHttpImpl;
import io.dropwizard.setup.Environment;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.apache.curator.framework.CuratorFramework;
import ru.vyarus.dropwizard.guice.module.yaml.bind.Config;

import javax.annotation.Nullable;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.function.Supplier;

@Slf4j
public class ClientModule extends AbstractModule {

    @Provides
    @Singleton
    public EventIngestorClient getEventIngestorClient(
            final @Config EventIngestorClientConfig eventIngestionConfig,
            final ServiceEndpointProviderFactory serviceEndpointProviderFactory,
            final Environment environment,
            final @OlympusAuthSupplier Supplier<String> authSupplier
    ) {
        try {
            return new EventIngestorClient(
                    eventIngestionConfig,
                    serviceEndpointProviderFactory,
                    environment.getObjectMapper(),
                    environment.metrics(),
                    authSupplier
            );
        } catch (Exception e) {
            throw initError("Error while creating event Ingestor Client", e);
        }
    }

    @Provides
    @Singleton
    @Named(BindingNames.SAMPLE_SERVICE)
    public HttpExecutorBuilderFactory getSampleServiceClient(
            final @Config("sampleAppConfiguration") HttpConfiguration configuration,
            final ServiceEndpointProviderFactory endpointProviderFactory,
            final Environment environment
    ) {
        return createHttpExecutorBuilderFactory(
                endpointProviderFactory,
                environment,
                configuration,
                SampleServiceHttpImpl.class
        );
    }

    private HttpExecutorBuilderFactory createHttpExecutorBuilderFactory(
            final ServiceEndpointProviderFactory endpointProviderFactory,
            final Environment environment,
            final HttpConfiguration httpConfiguration,
            final Class<?> callerClass
    ) {
        return HttpExecutorBuilderFactory.builder()
                .client(createHttpClient(httpConfiguration, environment.metrics()))
                .mapper(environment.getObjectMapper())
                .endpointProvider(endpointProviderFactory.provider(httpConfiguration, environment))
                .callerClass(callerClass)
                .build();
    }

    private OkHttpClient createHttpClient(
            final HttpConfiguration httpConfiguration,
            final MetricRegistry metricRegistry
    ) {
        try {
            return ClientFactory.newHttpClientBuilder()
                    .withMetricRegistry(metricRegistry)
                    .withConfiguration(httpConfiguration)
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            throw initError("Error while creating OkHttpClient for '%s'".formatted(httpConfiguration.getClientId()), e);
        }
    }

    private AtomDbError initError(final String message, final Exception e) {
        log.error(message, e);
        return AtomDbError.propagate(e)
                .errorCode(AtomDbErrorCode.INIT_ERROR)
                .build();
    }
}
