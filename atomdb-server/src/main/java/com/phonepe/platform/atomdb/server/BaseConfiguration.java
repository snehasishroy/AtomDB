package com.phonepe.platform.atomdb.server;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hystrix.configurator.config.HystrixConfig;
import com.phonepe.dataplatform.EventIngestorClientConfig;
import com.phonepe.metrics.config.ReporterConfig;
import com.phonepe.olympus.im.client.config.OlympusIMClientConfig;
import com.phonepe.platform.http.v2.common.hub.RangerHubConfiguration;
import com.platform.validation.ValidationConfig;
import in.vectorpro.dropwizard.swagger.SwaggerBundleConfiguration;
import io.appform.ranger.discovery.bundle.ServiceDiscoveryConfiguration;
import io.dropwizard.Configuration;
import lombok.Data;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@JsonDeserialize(as = AtomDbConfiguration.class)
public abstract class BaseConfiguration extends Configuration {

    @Valid
    @NotNull
    private HystrixConfig hystrixConfig = new HystrixConfig();

    @Valid
    @NotNull
    private SwaggerBundleConfiguration swagger = new SwaggerBundleConfiguration();

    @Valid
    @NotNull
    private ServiceDiscoveryConfiguration discovery = new ServiceDiscoveryConfiguration();

    @Valid
    @NotNull
    private EventIngestorClientConfig eventIngestor = new EventIngestorClientConfig();

    private boolean eventIngestionDisabled = false;

    @Valid
    @NotNull
    private ValidationConfig validationConfig;

    @Valid
    @NotNull
    private OlympusIMClientConfig olympusIMClientConfig;

    @Valid
    private ReporterConfig reporterConfig;

    @Valid
    @NotNull
    private RangerHubConfiguration rangerHubConfiguration;

}
