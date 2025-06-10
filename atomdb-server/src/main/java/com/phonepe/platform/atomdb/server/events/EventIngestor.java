package com.phonepe.platform.atomdb.server.events;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.phonepe.dataplatform.EventIngestorClient;
import com.phonepe.platform.eventingestion.model.Event;
import com.phonepe.platform.atomdb.server.guice.bindings.BindingNames;
import io.dropwizard.lifecycle.Managed;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Supplier;

@Slf4j
@Singleton
@AllArgsConstructor(onConstructor_ = @Inject)
public class EventIngestor implements Managed {

    private final EventIngestorClient eventIngestorClient;
    @Named(BindingNames.EVENT_INGESTION)
    private final Supplier<Boolean> eventIngestionDisabledSupplier;

    @Override
    public void start() throws Exception {
        eventIngestorClient.start();
        log.info("Started event ingestion client");
    }

    @Override
    public void stop() throws Exception {
        eventIngestorClient.close();
        log.info("Stopped event ingestion client");
    }

    public void send(final Event<?> event) {
        send(List.of(event));
    }

    public void send(final List<Event<?>> events) {
        if (null == eventIngestorClient || eventIngestionDisabledSupplier.get() || events == null) {
            return;
        }
        try {
            eventIngestorClient.send(List.copyOf(events));
        } catch (Exception e) {
            log.error("Error sending events: " + events, e);
        }
    }
}
