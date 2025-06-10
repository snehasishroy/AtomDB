package com.phonepe.platform.atomdb.server.events.type;

import static org.junit.jupiter.api.Assertions.*;

import com.phonepe.platform.atomdb.server.events.EventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SampleServiceCallErrorEventTest {

    private SampleServiceCallErrorEvent event;

    @BeforeEach
    void setUp() {
        event = SampleServiceCallErrorEvent.builder()
                .serviceName("sampleService")
                .responseCode(500)
                .message("Internal server error")
                .build();
    }

    @Test
    @DisplayName("Test getEventType() returns correct value")
    void testGetEventType() {
        assertEquals(EventType.EXCEPTION, event.getEventType());
    }

    @Test
    @DisplayName("Test getServiceName() returns correct value")
    void testGetServiceName() {
        assertEquals("sampleService", event.getServiceName());
    }

    @Test
    @DisplayName("Test getResponseCode() returns correct value")
    void testGetResponseCode() {
        assertEquals(500, event.getResponseCode());
    }

    @Test
    @DisplayName("Test getMessage() returns correct value")
    void testGetMessage() {
        assertEquals("Internal server error", event.getMessage());
    }
}
