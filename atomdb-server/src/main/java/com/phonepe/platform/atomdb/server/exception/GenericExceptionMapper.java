package com.phonepe.platform.atomdb.server.exception;

import com.phonepe.platform.atomdb.models.AtomDbResponse;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Slf4j
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(final Exception e) {
        if (e instanceof WebApplicationException webApplicationException) {
            Response response = webApplicationException.getResponse();
            return Response
                    .status(response.getStatus())
                    .entity(AtomDbResponse.builder()
                                    .success(false)
                                    .errorCode(response.getStatus())
                                    .errorMessage(webApplicationException.getMessage())
                                    .build())
                    .build();
        }
        log.error("Error handling request", e);
        return Response.serverError()
                .entity(AtomDbResponse.builder()
                        .success(false)
                        .errorCode(AtomDbErrorCode.INTERNAL_SERVER_ERROR.getCode())
                        .errorMessage(AtomDbErrorCode.INTERNAL_SERVER_ERROR.getMessage())
                        .build())
                .build();
    }
}
