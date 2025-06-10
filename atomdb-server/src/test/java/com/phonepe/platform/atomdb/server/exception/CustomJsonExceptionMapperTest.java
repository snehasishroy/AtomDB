package com.phonepe.platform.atomdb.server.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.phonepe.platform.atomdb.server.exception.AtomDbErrorCode;
import com.phonepe.platform.atomdb.models.AtomDbResponse;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;

class CustomJsonExceptionMapperTest {

    @Test
    @DisplayName("Test if toResponse() returns server error response with correct error code and message")
    void testToResponse() {
        CustomJsonExceptionMapper mapper = new CustomJsonExceptionMapper();
        JsonProcessingException exception = new JsonProcessingException("Test exception") {};
        Response response = mapper.toResponse(exception);
        Assertions.assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        AtomDbResponse AtomDbResponse = (AtomDbResponse) response.getEntity();
        Assertions.assertFalse(AtomDbResponse.isSuccess());
        Assertions.assertEquals(AtomDbErrorCode.JSON_SER_ERROR.getCode(), AtomDbResponse.getErrorCode());
        Assertions.assertEquals(ExceptionUtils.getRootCauseMessage(exception), AtomDbResponse.getErrorMessage());
    }
}
