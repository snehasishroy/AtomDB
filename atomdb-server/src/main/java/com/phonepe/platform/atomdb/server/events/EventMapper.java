package com.phonepe.platform.atomdb.server.events;

import com.google.common.base.Strings;
import com.phonepe.platform.eventingestion.model.Event;
import com.phonepe.platform.eventingestion.model.EventCriticalityBucket;
import com.phonepe.platform.eventingestion.model.EventTTLBucket;
import com.phonepe.platform.atomdb.server.AtomDbApplication;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@UtilityClass
public class EventMapper {

    public final String DEFAULT_VERSION = "v1";

    public <T> Event<T> event(final EventType eventType, final T data, final String groupingKey) {
        return Event.<T>builder()
                .app(AtomDbApplication.APP_NAME)
                .id(UUID.randomUUID().toString())
                .eventSchemaVersion(DEFAULT_VERSION)
                .eventType(eventType.name())
                .eventData(data)
                .time(Date.from(Instant.now()))
                .groupingKey(Strings.isNullOrEmpty(groupingKey) ? UUID.randomUUID().toString() : groupingKey)
                .eventCriticalityBucket(EventCriticalityBucket.LOW)
                .eventTTLBucket(EventTTLBucket.LOW)
                .build();
    }

    public <T extends BaseEvent> Event<T> event(final T baseEvent) {
        return event(baseEvent, null);
    }

    public <T extends BaseEvent> Event<T> event(final T baseEvent, final String groupingKey) {
        return event(baseEvent.getEventType(), baseEvent, groupingKey);
    }
}
