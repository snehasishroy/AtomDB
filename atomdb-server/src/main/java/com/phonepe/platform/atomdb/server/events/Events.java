package com.phonepe.platform.atomdb.server.events;

import com.phonepe.platform.eventingestion.model.Event;
import com.phonepe.platform.atomdb.server.events.type.AtomDbErrorEvent;
import com.phonepe.platform.atomdb.server.events.type.SampleServiceCallErrorEvent;
import com.phonepe.platform.atomdb.server.exception.AtomDbError;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Events {

    public Event<AtomDbErrorEvent> createExceptionEvent(final Exception e) {
        final AtomDbError error = AtomDbError.propagate(e).build();
        return EventMapper.event(AtomDbErrorEvent.builder()
                .errorCode(error.getErrorCode())
                .message(error.getMessage())
                .build());
    }

    public Event<SampleServiceCallErrorEvent> createServiceExceptionEvent(
            final String serviceName,
            final int responseCode,
            final String message
    ) {
        return EventMapper.event(SampleServiceCallErrorEvent.builder()
                .serviceName(serviceName)
                .responseCode(responseCode)
                .message(message)
                .build());
    }
}
