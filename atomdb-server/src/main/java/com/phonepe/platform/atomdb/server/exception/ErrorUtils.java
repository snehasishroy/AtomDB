package com.phonepe.platform.atomdb.server.exception;

import lombok.experimental.UtilityClass;
import org.apache.commons.text.StringSubstitutor;

import java.util.Map;

@UtilityClass
public class ErrorUtils {

    public String message(final AtomDbErrorCode errorCode, final Map<String, Object> context) {
        return String.format("Error [%d]: %s", errorCode.getCode(),
                new StringSubstitutor(context).replace(errorCode.getMessage()));
    }
}
