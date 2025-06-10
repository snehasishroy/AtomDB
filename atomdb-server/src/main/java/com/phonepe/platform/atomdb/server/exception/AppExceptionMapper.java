package com.phonepe.platform.atomdb.server.exception;

import com.phonepe.platform.atomdb.models.AtomDbResponse;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Slf4j
@Provider
public class AppExceptionMapper implements ExceptionMapper<AtomDbError> {

    @Override
    public Response toResponse(final AtomDbError error) {
        log.error("Error handling request: ", error);
        return Response.status(error.getErrorCode().getHttpStatusCode())
                .entity(AtomDbResponse.builder()
                        .success(error.getErrorCode() == AtomDbErrorCode.SUCCESS)
                        .errorCode(error.getErrorCode().getCode())
                        .errorMessage(error.getParsedMessage())
                        .build())
                .build();
    }
}
