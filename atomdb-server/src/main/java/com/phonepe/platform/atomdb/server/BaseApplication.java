package com.phonepe.platform.atomdb.server;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.phonepe.data.provider.rosey.bundle.RoseyConfigProviderBundle;
import com.phonepe.metrics.MetricIngestionBundle;
import com.phonepe.metrics.config.ReporterConfig;
import com.phonepe.olympus.im.client.OlympusIMBundle;
import com.phonepe.olympus.im.client.config.OlympusIMClientConfig;
import com.phonepe.platform.http.v2.common.hub.RangerHubConfiguration;
import com.phonepe.platform.http.v2.discovery.HttpDiscoveryBundle;
import com.phonepe.platform.http.v2.discovery.ServiceEndpointProviderFactory;
import com.phonepe.platform.requestinfo.bundle.RequestInfoBundle;
import com.platform.validation.ValidationBundle;
import com.platform.validation.ValidationConfig;
import in.vectorpro.dropwizard.swagger.SwaggerBundle;
import in.vectorpro.dropwizard.swagger.SwaggerBundleConfiguration;
import io.appform.ranger.discovery.bundle.ServiceDiscoveryBundle;
import io.appform.ranger.discovery.bundle.ServiceDiscoveryConfiguration;
import io.dropwizard.Application;
import io.dropwizard.oor.OorBundle;
import org.apache.curator.framework.CuratorFramework;
import org.zapodot.hystrix.bundle.HystrixBundle;
import ru.vyarus.dropwizard.guice.GuiceBundle;

import java.util.function.Supplier;

abstract class BaseApplication<T extends BaseConfiguration> extends Application<T> {

    HystrixBundle<T> hystrixBundle() {
        // noinspection unchecked
        return HystrixBundle.builder()
                .disableStreamServletInAdminContext()
                .withApplicationStreamPath("/hystrix.stream")
                .build();
    }

    SwaggerBundle<T> swaggerBundle() {
        return new SwaggerBundle<>() {
            @Override
            protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(T configuration) {
                return configuration.getSwagger();
            }
        };
    }

    ServiceDiscoveryBundle<T> serviceDiscoveryBundle(final String serviceName) {
        return new ServiceDiscoveryBundle<>() {
            @Override
            protected ServiceDiscoveryConfiguration getRangerConfiguration(T configuration) {
                return configuration.getDiscovery();
            }

            @Override
            protected String getServiceName(T configuration) {
                return serviceName;
            }
        };
    }

    OorBundle<T> oorBundle() {
        return new OorBundle<>() {
            @Override
            public boolean withOor() {
                return false;
            }
        };
    }

    GuiceBundle guiceBundle(Module... modules) {
        return GuiceBundle.builder()
                .enableAutoConfig(getClass().getPackage().getName())
                .modules(modules)
                .build(Stage.PRODUCTION);
    }

    RoseyConfigProviderBundle<T> appConfigProviderBundle(
            final String roseyPath,
            final String teamId,
            final String configName
    ) {
        return new RoseyConfigProviderBundle<>() {
            @Override
            public String getRoseyConfigPath(T configuration) {
                return roseyPath;
            }

            @Override
            public String getRoseyTeamId(T configuration) {
                return teamId;
            }

            @Override
            public String getRoseyConfigName(T configuration) {
                return configName;
            }
        };
    }

    OlympusIMBundle<AtomDbConfiguration> olympusIMBundle(
            final ServiceDiscoveryBundle<AtomDbConfiguration> serviceDiscoveryBundle,
            final Supplier<Injector> injectorSupplier
    ) {
        return new OlympusIMBundle<>() {
            @Override
            protected CuratorFramework getCuratorFramework() {
                return serviceDiscoveryBundle.getCurator();
            }

            @Override
            protected OlympusIMClientConfig getOlympusIMClientConfig(AtomDbConfiguration configuration) {
                return configuration.getOlympusIMClientConfig();
            }

            @Override
            protected Supplier<Injector> getGuiceInjector() {
                return injectorSupplier;
            }
        };
    }

    MetricIngestionBundle<T> metricIngestionBundle(
            final HttpDiscoveryBundle<T> httpDiscoveryBundle,
            final OlympusIMBundle<T> olympusIMBundle
    ) {
        return new MetricIngestionBundle<>() {
            @Override
            public ReporterConfig reporterConfig(T configuration) {
                return configuration.getReporterConfig();
            }

            @Override
            public Supplier<String> authTokenSupplier(T configuration) {
                return () -> olympusIMBundle.getOlympusIMClient().getSystemAuthHeader();
            }

            @Override
            public boolean registerMicrometer() {
                return false;
            }

            @Override
            public ServiceEndpointProviderFactory getServiceEndpointProviderFactory(T t) {
                return httpDiscoveryBundle.getEndpointProviderFactory();
            }
        };
    }

    ValidationBundle<T> validationBundle() {
        return new ValidationBundle<>() {
            @Override
            public ValidationConfig getValidationConfig(T configuration) {
                return configuration.getValidationConfig();
            }
        };
    }

    HttpDiscoveryBundle<AtomDbConfiguration> httpDiscoveryBundle(
            ServiceDiscoveryBundle<AtomDbConfiguration> serviceDiscoveryBundle) {
        return new HttpDiscoveryBundle<>() {
            @Override
            protected CuratorFramework getCuratorFramework(AtomDbConfiguration configuration) {
                return serviceDiscoveryBundle.getCurator();
            }

            @Override
            protected RangerHubConfiguration getHubConfiguration(
                    AtomDbConfiguration configuration) {
                return configuration.getRangerHubConfiguration();
            }
        };
    }

    RequestInfoBundle<AtomDbConfiguration> requestInfoBundle(
            HttpDiscoveryBundle<AtomDbConfiguration> discoveryBundle) {
        return new RequestInfoBundle<>() {
            @Override
            protected ServiceEndpointProviderFactory getEndpointProviderFactory() {
                return discoveryBundle.getEndpointProviderFactory();
            }
        };
    }
}
