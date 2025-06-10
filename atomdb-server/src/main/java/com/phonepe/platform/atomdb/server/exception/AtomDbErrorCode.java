package com.phonepe.platform.atomdb.server.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import javax.ws.rs.core.Response;

@Getter
@AllArgsConstructor
public enum AtomDbErrorCode {

    SUCCESS(0, "Success", Response.Status.OK),

    /* 1xxx for internal errors */
    INTERNAL_SERVER_ERROR(1000, "Internal Server Error", Response.Status.INTERNAL_SERVER_ERROR),
    WEB_APP_ERROR(1001, "Internal Web App Error", Response.Status.INTERNAL_SERVER_ERROR),
    JSON_SER_ERROR(1002, "Internal serialization Error", Response.Status.INTERNAL_SERVER_ERROR),
    JSON_DESER_ERROR(1003, "Internal deserialization Error", Response.Status.INTERNAL_SERVER_ERROR),
    DB_ERROR(1004, "Internal Database error", Response.Status.INTERNAL_SERVER_ERROR),

    /* 2xxx for auth and permissions */
    AUTH_ERROR(2000, "Authorization Error", Response.Status.UNAUTHORIZED),
    FORBIDDEN_ERROR(2001, "Forbidden Error", Response.Status.FORBIDDEN),
    NOT_FOUND_ERROR(2002, "Not Found", Response.Status.NOT_FOUND),
    OPERATION_NOT_ALLOWED(2003, "Operation is not allowed", Response.Status.METHOD_NOT_ALLOWED),

    /* 3xxx for validation */
    WRONG_INPUT_ERROR(3000, "Invalid Input", Response.Status.BAD_REQUEST),

    /* 4xxx for client errors */
    CLIENT_FAILURE(4000, "Failed to call client", Response.Status.INTERNAL_SERVER_ERROR),

    /* 5xxx for start-up errors */
    INIT_ERROR(5000, "Unable to initialize", Response.Status.INTERNAL_SERVER_ERROR),
    ;

    private final int code;
    private final String message;
    private final Response.Status httpStatusCode;
}
