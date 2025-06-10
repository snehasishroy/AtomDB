package com.phonepe.platform.atomdb.server.guice;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.phonepe.data.provider.rosey.bundle.RoseyConfigProviderBundle;
import com.phonepe.platform.atomdb.server.AtomDbConfiguration;
import com.phonepe.platform.http.v2.discovery.HttpDiscoveryBundle;
import com.phonepe.platform.http.v2.discovery.ServiceEndpointProviderFactory;
import io.appform.ranger.discovery.bundle.ServiceDiscoveryBundle;
import lombok.AllArgsConstructor;
import org.apache.curator.framework.CuratorFramework;

import java.util.function.Supplier;

@AllArgsConstructor
public class ServiceModule extends AbstractModule {

    private final HttpDiscoveryBundle<AtomDbConfiguration> discoveryBundle;
    private final ServiceDiscoveryBundle<AtomDbConfiguration> serviceDiscoveryBundle;
    private final RoseyConfigProviderBundle<AtomDbConfiguration> roseyConfigProviderBundle;
    private final ObjectMapper objectMapper;
    private final MetricRegistry metricRegistry;

    @Override
    protected void configure() {
        bind(CuratorFramework.class).toInstance(serviceDiscoveryBundle.getCurator());
    }

    @Provides
    @Singleton
    public ServiceEndpointProviderFactory getEndpointFactory() {
        return discoveryBundle.getEndpointProviderFactory();
    }

    @Provides
    public ServiceDiscoveryBundle<AtomDbConfiguration> getServiceDiscoveryBundle() {
        return serviceDiscoveryBundle;
    }

    @Provides
    @Singleton
    public MetricRegistry metricRegistry() {
        return metricRegistry;
    }

    @Provides
    @Singleton
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Provides
    @Singleton
    public Supplier<AtomDbConfiguration> getDataProvider(final AtomDbConfiguration initialConfiguration) {
        final var provider = roseyConfigProviderBundle.getDataProvider();
        if (provider != null) {
            return provider::getData;
        }
        return () -> initialConfiguration;
    }
}
