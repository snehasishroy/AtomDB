package com.phonepe.platform.atomdb.server.common;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ConstantsTest {
    @Test
    void testHostname() {
        String expected = System.getenv("HOST");
        if (expected == null || expected.isEmpty()) {
            expected = "UNKNOWN";
        }

        String actual = Constants.HOSTNAME;
        assertEquals(expected, actual);
    }
}
