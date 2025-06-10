package com.phonepe.platform.atomdb.models;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class AtomDbResponse<T> {
    @Builder.Default
    private boolean success = true;

    private T response;

    private int errorCode;

    private String errorMessage;

    public static <T> AtomDbResponse<T> ok(final T response) {
        return AtomDbResponse.<T>builder()
                .response(response)
                .build();
    }
}