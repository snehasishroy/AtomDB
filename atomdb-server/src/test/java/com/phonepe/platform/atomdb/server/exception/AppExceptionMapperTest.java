package com.phonepe.platform.atomdb.server.exception;

import com.phonepe.platform.atomdb.server.exception.AtomDbErrorCode;
import com.phonepe.platform.atomdb.models.AtomDbResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.core.Response;

@DisplayName("AppExceptionMapper Tests")
@ExtendWith(MockitoExtension.class)
class AppExceptionMapperTest {

    private AtomDbError error = AtomDbError
            .raise(AtomDbErrorCode.INTERNAL_SERVER_ERROR)
            .message("An internal error occurred")
            .build();

    @Test
    @DisplayName("Test if toResponse returns the expected response")
    void testToResponseReturnsExpectedResponse() {
        // Create an instance of AppExceptionMapper
        AppExceptionMapper appExceptionMapper = new AppExceptionMapper();

        // Call the toResponse method and check the returned response
        Response response = appExceptionMapper.toResponse(error);

        Assertions.assertEquals(response.getStatus(), AtomDbErrorCode.INTERNAL_SERVER_ERROR.getHttpStatusCode().getStatusCode());

        AtomDbResponse AtomDbResponse = (AtomDbResponse) response.getEntity();
        Assertions.assertEquals(AtomDbErrorCode.INTERNAL_SERVER_ERROR.getCode(), AtomDbResponse.getErrorCode());
        Assertions.assertEquals("Error [1000]: Internal Server Error", AtomDbResponse.getErrorMessage());
        Assertions.assertFalse(AtomDbResponse.isSuccess());
    }
}
