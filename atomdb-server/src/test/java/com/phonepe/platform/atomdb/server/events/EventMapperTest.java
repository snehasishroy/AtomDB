package com.phonepe.platform.atomdb.server.events;

import com.phonepe.platform.eventingestion.model.Event;
import com.phonepe.platform.atomdb.server.AtomDbApplication;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventMapperTest {

    @Test
    void testEventWithGroupingKey() {
        String groupingKey = "groupingKey";
        EventType eventType = EventType.EXCEPTION;
        String data = "user123";
        Event<String> event = EventMapper.event(eventType, data, groupingKey);

        assertEquals(AtomDbApplication.APP_NAME, event.getApp());
        assertNotNull(event.getId());
        assertEquals(EventMapper.DEFAULT_VERSION, event.getEventSchemaVersion());
        assertEquals(eventType.name(), event.getEventType());
        assertEquals(data, event.getEventData());
        assertTrue(Date.from(Instant.now()).getTime() - event.getTime().getTime() < 1000);
        assertEquals(groupingKey, event.getGroupingKey());
    }

    @Test
    void testEventWithoutGroupingKey() {
        EventType eventType = EventType.EXCEPTION;
        Integer data = 123;
        Event<Integer> event = EventMapper.event(eventType, data, null);

        assertEquals(AtomDbApplication.APP_NAME, event.getApp());
        assertNotNull(event.getId());
        assertEquals(EventMapper.DEFAULT_VERSION, event.getEventSchemaVersion());
        assertEquals(eventType.name(), event.getEventType());
        assertEquals(data, event.getEventData());
        assertTrue(Date.from(Instant.now()).getTime() - event.getTime().getTime() < 1000);
        assertNotNull(event.getGroupingKey());
    }

    @Test
    void testEventWithBaseEventAndGroupingKey() {
        String groupingKey = "groupingKey";
        EventType eventType = EventType.EXCEPTION;
        BaseEvent data = () -> eventType;
        Event<BaseEvent> event = EventMapper.event(data, groupingKey);

        assertEquals(AtomDbApplication.APP_NAME, event.getApp());
        assertNotNull(event.getId());
        assertEquals(EventMapper.DEFAULT_VERSION, event.getEventSchemaVersion());
        assertEquals(eventType.name(), event.getEventType());
        assertEquals(data, event.getEventData());
        assertTrue(Date.from(Instant.now()).getTime() - event.getTime().getTime() < 1000);
        assertEquals(groupingKey, event.getGroupingKey());
    }

    @Test
    void testEventWithBaseEventWithoutGroupingKey() {
        EventType eventType = EventType.EXCEPTION;
        BaseEvent data = () -> eventType;
        Event<BaseEvent> event = EventMapper.event(data);

        assertEquals(AtomDbApplication.APP_NAME, event.getApp());
        assertNotNull(event.getId());
        assertEquals(EventMapper.DEFAULT_VERSION, event.getEventSchemaVersion());
        assertEquals(eventType.name(), event.getEventType());
        assertEquals(data, event.getEventData());
        assertTrue(Date.from(Instant.now()).getTime() - event.getTime().getTime() < 1000);
        assertNotNull(event.getGroupingKey());
    }
}
