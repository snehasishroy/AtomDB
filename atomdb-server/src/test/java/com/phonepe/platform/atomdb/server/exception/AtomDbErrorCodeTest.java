package com.phonepe.platform.atomdb.server.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AtomDbErrorCodeTest {

    @Test
    void testSuccess() {
        AtomDbErrorCode code = AtomDbErrorCode.SUCCESS;
        Assertions.assertEquals(0, code.getCode());
        Assertions.assertEquals("Success", code.getMessage());
        Assertions.assertEquals(200, code.getHttpStatusCode().getStatusCode());
    }

    @Test
    void testInternalServerError() {
        AtomDbErrorCode code = AtomDbErrorCode.INTERNAL_SERVER_ERROR;
        Assertions.assertEquals(1000, code.getCode());
        Assertions.assertEquals("Internal Server Error", code.getMessage());
        Assertions.assertEquals(500, code.getHttpStatusCode().getStatusCode());
    }

    @Test
    void testWebAppError() {
        AtomDbErrorCode code = AtomDbErrorCode.WEB_APP_ERROR;
        Assertions.assertEquals(1001, code.getCode());
        Assertions.assertEquals("Internal Web App Error", code.getMessage());
        Assertions.assertEquals(500, code.getHttpStatusCode().getStatusCode());
    }

    @Test
    void testJsonSerError() {
        AtomDbErrorCode code = AtomDbErrorCode.JSON_SER_ERROR;
        Assertions.assertEquals(1002, code.getCode());
        Assertions.assertEquals("Internal serialization Error", code.getMessage());
        Assertions.assertEquals(500, code.getHttpStatusCode().getStatusCode());
    }

    @Test
    void testJsonDeserError() {
        AtomDbErrorCode code = AtomDbErrorCode.JSON_DESER_ERROR;
        Assertions.assertEquals(1003, code.getCode());
        Assertions.assertEquals("Internal deserialization Error", code.getMessage());
        Assertions.assertEquals(500, code.getHttpStatusCode().getStatusCode());
    }

    @Test
    void testDbError() {
        AtomDbErrorCode code = AtomDbErrorCode.DB_ERROR;
        Assertions.assertEquals(1004, code.getCode());
        Assertions.assertEquals("Internal Database error", code.getMessage());
        Assertions.assertEquals(500, code.getHttpStatusCode().getStatusCode());
    }

    @Test
    void testAuthError() {
        AtomDbErrorCode code = AtomDbErrorCode.AUTH_ERROR;
        Assertions.assertEquals(2000, code.getCode());
        Assertions.assertEquals("Authorization Error", code.getMessage());
        Assertions.assertEquals(401, code.getHttpStatusCode().getStatusCode());
    }

    @Test
    void testForbiddenError() {
        AtomDbErrorCode code = AtomDbErrorCode.FORBIDDEN_ERROR;
        Assertions.assertEquals(2001, code.getCode());
        Assertions.assertEquals("Forbidden Error", code.getMessage());
        Assertions.assertEquals(403, code.getHttpStatusCode().getStatusCode());
    }

    @Test
    void testOperationNotAllowed() {
        AtomDbErrorCode code = AtomDbErrorCode.OPERATION_NOT_ALLOWED;
        Assertions.assertEquals(2003, code.getCode());
        Assertions.assertEquals("Operation is not allowed", code.getMessage());
        Assertions.assertEquals(405, code.getHttpStatusCode().getStatusCode());
    }

    @Test
    void testWrongInputError() {
        AtomDbErrorCode code = AtomDbErrorCode.WRONG_INPUT_ERROR;
        Assertions.assertEquals(3000, code.getCode());
        Assertions.assertEquals("Invalid Input", code.getMessage());
        Assertions.assertEquals(400, code.getHttpStatusCode().getStatusCode());
    }

    @Test
    void testInitError() {
        AtomDbErrorCode code = AtomDbErrorCode.INIT_ERROR;
        Assertions.assertEquals(5000, code.getCode());
        Assertions.assertEquals("Unable to initialize", code.getMessage());
        Assertions.assertEquals(500, code.getHttpStatusCode().getStatusCode());
    }
}
