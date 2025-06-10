package com.phonepe.platform.atomdb.server.services.impl;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.phonepe.platform.atomdb.server.AtomDbConfiguration;
import com.phonepe.platform.atomdb.server.events.EventIngestor;
import com.phonepe.platform.atomdb.server.events.Events;
import com.phonepe.platform.atomdb.server.guice.bindings.BindingNames;
import com.phonepe.platform.atomdb.server.services.SampleService;
import com.phonepe.platform.http.v2.common.HttpConfiguration;
import com.phonepe.platform.http.v2.executor.ExtractedResponse;
import com.phonepe.platform.http.v2.executor.factory.HttpExecutorBuilderFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.vyarus.dropwizard.guice.module.yaml.bind.Config;

@Slf4j
@AllArgsConstructor(onConstructor_ = @Inject)
public class SampleServiceHttpImpl implements SampleService {

    private final EventIngestor eventIngestor;
    @Config(AtomDbConfiguration.Fields.SAMPLE_APP_CONFIGURATION)
    private final HttpConfiguration clientConfiguration;
    @Named(BindingNames.SAMPLE_SERVICE)
    private final HttpExecutorBuilderFactory executorFactory;

    @Override
    public boolean sampleApi(final String sampleParam) {
        return executorFactory.<Boolean>httpGetExecutorBuilder()
                .responseType(Boolean.class)
                .url("/v1/some/path?queryParam=" + sampleParam)
                .command("sampleApi")
                .nonSuccessResponseConsumer(extractedResponse ->
                        handleNonSuccessResponse(extractedResponse, sampleParam, false))
                .build()
                .executeTracked();
    }

    private <T> T handleNonSuccessResponse(
            final ExtractedResponse extractedResponse,
            final Object context,
            final T defaultValue
    ) {
        log.error("[nonSuccessResponseConsumer] Error while hitting sample api for:{} responseCode:{} body:{}",
                context, extractedResponse.getCode(), new String(extractedResponse.getBody()));
        eventIngestor.send(Events.createServiceExceptionEvent(
                clientConfiguration.getServiceName(),
                extractedResponse.getCode(),
                extractedResponse.getMessage()
        ));
        return defaultValue;
    }
}
