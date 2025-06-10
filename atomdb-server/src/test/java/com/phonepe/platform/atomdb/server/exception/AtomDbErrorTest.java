package com.phonepe.platform.atomdb.server.exception;

import com.phonepe.platform.atomdb.server.exception.AtomDbErrorCode;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class AtomDbErrorTest {

    @Test
    void testAtomDbErrorBuilder_build() {
        AtomDbError error = AtomDbError.raise(AtomDbErrorCode.INTERNAL_SERVER_ERROR)
                .message("Something went wrong")
                .cause(new Exception("Error occurred"))
                .context(Collections.singletonMap("key", "value"))
                .build();

        assertEquals(AtomDbErrorCode.INTERNAL_SERVER_ERROR, error.getErrorCode());
        assertEquals("Something went wrong Error: Error occurred", error.getMessage());
        assertEquals(Collections.singletonMap("key", "value"), error.getContext());
    }

    @Test
    void testAtomDbErrorBuilder_build_withAtomDbErrorCause() {
        AtomDbError serviceError = AtomDbError.raise(AtomDbErrorCode.INTERNAL_SERVER_ERROR).build();
        AtomDbError error = AtomDbError.propagate(serviceError).build();

        assertEquals(AtomDbErrorCode.INTERNAL_SERVER_ERROR, error.getErrorCode());
    }

    @Test
    void testAtomDbErrorBuilder_build_withCustomMessage() {
        AtomDbError error = AtomDbError.raise(AtomDbErrorCode.INTERNAL_SERVER_ERROR)
                .message("Custom message")
                .build();

        assertEquals("Custom message", error.getMessage());
    }

    @Test
    void testAtomDbErrorBuilder_build_withoutCustomMessage() {
        AtomDbError error = AtomDbError.raise(AtomDbErrorCode.INTERNAL_SERVER_ERROR).build();

        assertNotNull(error.getMessage());
    }

    @Test
    void testAtomDbErrorBuilder_build_withoutCause() {
        AtomDbError error = AtomDbError.raise(AtomDbErrorCode.INTERNAL_SERVER_ERROR)
                .message("Something went wrong")
                .build();

        assertEquals("Something went wrong", error.getMessage());
    }

    @Test
    void testAtomDbErrorBuilder_build_withCause() {
        AtomDbError error = AtomDbError.raise(AtomDbErrorCode.INTERNAL_SERVER_ERROR)
                .message("Something went wrong")
                .cause(new Exception("Error occurred"))
                .build();

        assertEquals("Something went wrong Error: Error occurred", error.getMessage());
    }

    @Test
    void testAtomDbErrorBuilder_build_withParsedMessage() {
        Map<String, Object> context = new HashMap<>();
        context.put("key", "value");
        AtomDbError error = AtomDbError.raise(AtomDbErrorCode.INTERNAL_SERVER_ERROR)
                .context(context)
                .build();

        assertNotNull(error.getParsedMessage());
    }
}
