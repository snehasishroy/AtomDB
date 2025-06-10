package com.phonepe.platform.atomdb.server;

import com.google.common.base.Strings;
import com.hystrix.configurator.core.HystrixConfigurationFactory;
import com.phonepe.data.provider.rosey.bundle.RoseyConfigProviderBundle;
import com.phonepe.olympus.im.client.OlympusIMBundle;
import com.phonepe.platform.http.server.metrics.bundle.MetricBundle;
import com.phonepe.platform.atomdb.server.exception.AppExceptionMapper;
import com.phonepe.platform.atomdb.server.exception.CustomJsonExceptionMapper;
import com.phonepe.platform.atomdb.server.exception.GenericExceptionMapper;
import com.phonepe.platform.atomdb.server.exception.ViolationExceptionMapper;
import com.phonepe.platform.atomdb.server.guice.ClientModule;
import com.phonepe.platform.atomdb.server.guice.ConfigModule;
import com.phonepe.platform.atomdb.server.guice.CoreModule;
import com.phonepe.platform.atomdb.server.guice.DBModule;
import com.phonepe.platform.atomdb.server.guice.ServiceModule;
import com.phonepe.platform.atomdb.server.utils.MapperUtils;
import com.phonepe.platform.http.v2.discovery.HttpDiscoveryBundle;
import com.phonepe.platform.requestinfo.bundle.RequestInfoBundle;
import com.phonepe.rosey.dwconfig.RoseyConfigSourceProvider;
import io.appform.ranger.discovery.bundle.ServiceDiscoveryBundle;
import io.appform.functionmetrics.FunctionMetricsManager;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.GuiceBundle;

public class AtomDbApplication extends BaseApplication<AtomDbConfiguration> {

    public static final String APP_NAME = "atomdb";
    private static final String TEAM_ID = "infra";
    private GuiceBundle guiceBundle;

    public static void main(String[] args) throws Exception {
        new AtomDbApplication().run(args);
    }

    @Override
    public String getName() {
        return APP_NAME;
    }

    @Override
    public void initialize(final Bootstrap<AtomDbConfiguration> bootstrap) {

        final var serviceName = getEnv("SERVICE_NAME", APP_NAME);
        final var serviceDiscoveryBundle = serviceDiscoveryBundle(serviceName);

        final var roseyPath = getEnv("ROSEY_PATH", "/rosey/config.yml");

        final var configName = getEnv("ROSEY_CONFIG_NAME", APP_NAME);
        final var roseyTeamId = getEnv("ROSEY_TEAM_ID", TEAM_ID);

        final var configProviderBundle = appConfigProviderBundle(roseyPath, roseyTeamId, configName);

        boolean localConfig = Boolean.parseBoolean(System.getProperty("localConfig", "false"));
        if (!localConfig) {
            bootstrap.addBundle(configProviderBundle);
        }
        bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
                localConfig ? bootstrap.getConfigurationSourceProvider()
                            : new RoseyConfigSourceProvider(roseyTeamId, configName),
                new EnvironmentVariableSubstitutor()));
        bootstrap.addBundle(new MetricBundle<>());
        bootstrap.addBundle(swaggerBundle());
        bootstrap.addBundle(oorBundle());
        bootstrap.addBundle(hystrixBundle());
        bootstrap.addBundle(serviceDiscoveryBundle);
        final var olympusIMBundle = olympusIMBundle(serviceDiscoveryBundle, () -> this.guiceBundle.getInjector());
        bootstrap.addBundle(olympusIMBundle);
        HttpDiscoveryBundle<AtomDbConfiguration> httpDiscoveryBundle = httpDiscoveryBundle(serviceDiscoveryBundle);
        bootstrap.addBundle(httpDiscoveryBundle);
        bootstrap.addBundle(requestInfoBundle(httpDiscoveryBundle));
        bootstrap.addBundle(metricIngestionBundle(httpDiscoveryBundle, olympusIMBundle));
        this.guiceBundle = createGuiceBundle(
                httpDiscoveryBundle,
                serviceDiscoveryBundle,
                configProviderBundle,
                olympusIMBundle,
                bootstrap
        );
        bootstrap.addBundle(guiceBundle);
        bootstrap.addBundle(validationBundle());
    }

    @Override
    public void run(final AtomDbConfiguration configuration, final Environment environment) {
        MapperUtils.configureMapper(environment.getObjectMapper());
        HystrixConfigurationFactory.init(configuration.getHystrixConfig());
        FunctionMetricsManager.initialize("commands", environment.metrics());
    }

    private GuiceBundle createGuiceBundle(
            HttpDiscoveryBundle<AtomDbConfiguration> discoveryBundle,
            ServiceDiscoveryBundle<AtomDbConfiguration> serviceDiscoveryBundle,
            RoseyConfigProviderBundle<AtomDbConfiguration> roseyConfigProviderBundle,
            OlympusIMBundle<AtomDbConfiguration> olympusIMBundle,
            Bootstrap<AtomDbConfiguration> bootstrap
    ) {
        return guiceBundle(
                new ServiceModule(
                        discoveryBundle,
                        serviceDiscoveryBundle,
                        roseyConfigProviderBundle,
                        bootstrap.getObjectMapper(),
                        bootstrap.getMetricRegistry()),
                new DBModule(),
                new CoreModule(olympusIMBundle),
                new ClientModule(),
                new ConfigModule()
        );
    }

    private String getEnv(String name, String defaultValue) {
        final var value = System.getenv(name);
        return Strings.isNullOrEmpty(value) ? defaultValue : value;
    }
}
