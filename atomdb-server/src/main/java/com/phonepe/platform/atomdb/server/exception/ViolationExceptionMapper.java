package com.phonepe.platform.atomdb.server.exception;

import com.phonepe.platform.atomdb.models.AtomDbResponse;
import io.dropwizard.jersey.validation.ConstraintMessage;
import io.dropwizard.jersey.validation.JerseyViolationException;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.stream.Collectors;

@Slf4j
@Provider
public class ViolationExceptionMapper implements ExceptionMapper<JerseyViolationException> {

    @Override
    public Response toResponse(final JerseyViolationException exception) {
        log.error("Object validation exception: ", exception);
        final var violations = exception.getConstraintViolations();
        final var invocable = exception.getInvocable();
        final var errorMessage = violations.stream()
                .map(violation -> ConstraintMessage.getMessage(violation, invocable))
                .collect(Collectors.joining(", "));
        int status = ConstraintMessage.determineStatus(violations, invocable);

        return Response.status(status)
                .entity(AtomDbResponse.builder()
                        .success(false)
                        .errorCode(AtomDbErrorCode.WRONG_INPUT_ERROR.getCode())
                        .errorMessage(errorMessage)
                        .build())
                .build();
    }
}
