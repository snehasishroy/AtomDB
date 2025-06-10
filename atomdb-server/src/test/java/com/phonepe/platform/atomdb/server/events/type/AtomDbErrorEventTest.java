package com.phonepe.platform.atomdb.server.events.type;

import com.phonepe.platform.atomdb.server.exception.AtomDbErrorCode;
import com.phonepe.platform.atomdb.server.events.EventType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AtomDbErrorEventTest {

    @Test
    void testGetEventType() {
        AtomDbErrorEvent event = AtomDbErrorEvent.builder()
                .errorCode(AtomDbErrorCode.INTERNAL_SERVER_ERROR)
                .message("Some error message")
                .build();
        Assertions.assertEquals(EventType.EXCEPTION, event.getEventType(), "EventType should be EXCEPTION");
    }

    @Test
    void testGetErrorCode() {
        AtomDbErrorEvent event = AtomDbErrorEvent.builder()
                .errorCode(AtomDbErrorCode.INTERNAL_SERVER_ERROR)
                .message("Some error message")
                .build();
        Assertions.assertEquals(AtomDbErrorCode.INTERNAL_SERVER_ERROR, event.getErrorCode(), "ErrorCode should be INTERNAL_SERVER_ERROR");
    }

    @Test
    void testGetMessage() {
        AtomDbErrorEvent event = AtomDbErrorEvent.builder()
                .errorCode(AtomDbErrorCode.INTERNAL_SERVER_ERROR)
                .message("Some error message")
                .build();
        Assertions.assertEquals("Some error message", event.getMessage(), "Message should be 'Some error message'");
    }
}
