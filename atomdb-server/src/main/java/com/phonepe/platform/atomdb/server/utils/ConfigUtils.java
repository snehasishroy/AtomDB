package com.phonepe.platform.atomdb.server.utils;

import java.util.function.Function;
import java.util.function.Supplier;
import lombok.experimental.UtilityClass;
import lombok.val;

@UtilityClass
public class ConfigUtils {

    public <T, R> Supplier<R> mappedSupplier(final Supplier<T> supplier,
                                             final Function<T, R> mapper) {
        return () -> mapper.apply(supplier.get());
    }

    public String readEnvOrProperty(String env,
                                    String property) {
        val envP = System.getenv(env);
        return envP == null || envP.isEmpty()
               ? System.getProperty(property)
               : envP;
    }
}
