package com.phonepe.platform.atomdb.server.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.phonepe.platform.eventingestion.model.Event;
import org.junit.jupiter.api.Test;

import com.phonepe.platform.atomdb.server.events.type.SampleServiceCallErrorEvent;
import com.phonepe.platform.atomdb.server.events.type.AtomDbErrorEvent;

class EventsTest {

    @Test
    void testCreateExceptionEvent() {
        final Exception exception = new Exception("Test Exception");

        final Event<AtomDbErrorEvent> event = Events.createExceptionEvent(exception);

        assertNotNull(event);
        assertEquals(EventType.EXCEPTION, event.getEventData().getEventType());
    }

    @Test
    void testCreateServiceExceptionEvent() {
        final String serviceName = "Test Service";
        final int responseCode = 404;
        final String message = "Not Found";

        final Event<SampleServiceCallErrorEvent> event = Events.createServiceExceptionEvent(serviceName, responseCode, message);

        assertNotNull(event);
        assertEquals("Test Service", event.getEventData().getServiceName());
        assertEquals(404, event.getEventData().getResponseCode());
        assertEquals("Not Found", event.getEventData().getMessage());
    }
}
