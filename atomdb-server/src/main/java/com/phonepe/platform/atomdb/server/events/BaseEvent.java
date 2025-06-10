package com.phonepe.platform.atomdb.server.events;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.phonepe.platform.atomdb.server.common.Constants;

public interface BaseEvent {
    default String getHostname() {
        return Constants.HOSTNAME;
    }

    @JsonIgnore
    EventType getEventType();
}
