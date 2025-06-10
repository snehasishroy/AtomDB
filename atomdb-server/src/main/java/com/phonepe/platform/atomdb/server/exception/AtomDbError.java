package com.phonepe.platform.atomdb.server.exception;

import io.dropwizard.util.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Map;

public class AtomDbError extends RuntimeException {
    @Getter
    private final AtomDbErrorCode errorCode;
    @Getter
    private final transient Map<String, Object> context;
    @Getter
    private final transient String parsedMessage;

    private AtomDbError(
            final AtomDbErrorCode errorCode,
            final String message,
            final Throwable cause,
            final Map<String, Object> context,
            final String parsedMessage
    ) {
        super(message, cause);
        this.errorCode = errorCode;
        this.context = context;
        this.parsedMessage = parsedMessage;
    }

    public static AtomDbErrorBuilder raise(final AtomDbErrorCode errorCode) {
        return new AtomDbErrorBuilder()
                .errorCode(errorCode);
    }

    public static AtomDbErrorBuilder propagate(final Throwable e) {
        return new AtomDbErrorBuilder()
                .cause(e);
    }

    @Setter
    @Accessors(fluent = true, chain = true)
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class AtomDbErrorBuilder {
        private Throwable cause = null;
        @NonNull
        private AtomDbErrorCode errorCode = AtomDbErrorCode.INTERNAL_SERVER_ERROR;
        private Map<String, Object> context = Map.of();
        private String message = null;

        public AtomDbError build() {
            if (cause instanceof AtomDbError error) {
                return error;
            }

            final var parsedMessage = ErrorUtils.message(errorCode, context);
            final var errorMessage = createMessage(parsedMessage);

            return new AtomDbError(errorCode, errorMessage, cause, context, parsedMessage);
        }

        private String createMessage(final String fallback) {
            if (cause == null) {
                return Strings.isNullOrEmpty(message) ? fallback : message;
            }
            final var errorMessage = "Error: " + cause.getMessage();
            if (Strings.isNullOrEmpty(message)) {
                return errorMessage;
            }
            return message + " " + errorMessage;
        }
    }
}
