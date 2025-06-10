package com.phonepe.platform.atomdb.server.utils;

import lombok.experimental.UtilityClass;

import java.util.function.Function;
import java.util.function.Supplier;

@UtilityClass
public class ConfigUtils {

    public <T, R> Supplier<R> mappedSupplier(final Supplier<T> supplier, final Function<T, R> mapper) {
        return () -> mapper.apply(supplier.get());
    }
}
