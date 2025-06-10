package com.phonepe.platform.atomdb.server.events.type;

import com.phonepe.platform.atomdb.server.exception.AtomDbErrorCode;
import com.phonepe.platform.atomdb.server.events.BaseEvent;
import com.phonepe.platform.atomdb.server.events.EventType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AtomDbErrorEvent implements BaseEvent {
    private final AtomDbErrorCode errorCode;
    private final String message;

    @Override
    public EventType getEventType() {
        return EventType.EXCEPTION;
    }
}
