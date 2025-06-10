package com.phonepe.platform.atomdb.server.events;

import com.phonepe.dataplatform.EventIngestorClient;
import com.phonepe.platform.eventingestion.model.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class EventIngestorTest {

    @Mock
    private EventIngestorClient eventIngestorClient;

    @Mock
    private Supplier<Boolean> eventIngestionDisabledSupplier;

    private EventIngestor eventIngestor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        eventIngestor = new EventIngestor(eventIngestorClient, eventIngestionDisabledSupplier);
    }

    @Test
    void testSendOneEvent() throws Exception {
        Event<?> event = mock(Event.class);
        when(eventIngestionDisabledSupplier.get()).thenReturn(false);
        eventIngestor.send(event);
        verify(eventIngestorClient).send(Collections.singletonList(event));
    }

    @Test
    void testSendMultipleEvents() throws Exception {
        Event<?> event1 = mock(Event.class);
        Event<?> event2 = mock(Event.class);
        when(eventIngestionDisabledSupplier.get()).thenReturn(false);
        eventIngestor.send(Arrays.asList(event1, event2));
        verify(eventIngestorClient).send(Arrays.asList(event1, event2));
    }

    @Test
    void testSendNullEventList() {
        when(eventIngestionDisabledSupplier.get()).thenReturn(false);
        eventIngestor.send((List<Event<?>>) null);
        verifyNoInteractions(eventIngestorClient);
    }

    @Test
    void testSendWithDisabledIngestion() {
        when(eventIngestionDisabledSupplier.get()).thenReturn(true);
        Event<?> event = mock(Event.class);
        eventIngestor.send(event);
        verifyNoInteractions(eventIngestorClient);
    }

    @Test
    void testStart() throws Exception {
        eventIngestor.start();
        verify(eventIngestorClient).start();
    }

    @Test
    void testStop() throws Exception {
        eventIngestor.stop();
        verify(eventIngestorClient).close();
    }
}
