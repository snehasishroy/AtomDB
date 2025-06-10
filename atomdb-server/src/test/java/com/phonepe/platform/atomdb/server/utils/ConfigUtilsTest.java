package com.phonepe.platform.atomdb.server.utils;

import org.junit.jupiter.api.Test;

import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigUtilsTest {

    @Test
    void mappedSupplier_withNullSupplier_throwsNullPointerException() {
        Supplier<String> supplier = null;
        Function<String, Integer> mapper = Integer::parseInt;
        Supplier<Integer> mappedSupplier = ConfigUtils.mappedSupplier(supplier, mapper);
        assertThrows(NullPointerException.class, () -> mappedSupplier.get());
    }

    @Test
    void mappedSupplier_withNullMapper_throwsNullPointerException() {
        Supplier<String> supplier = () -> "123";
        Function<String, Integer> mapper = null;
        Supplier<Integer> mappedSupplier = ConfigUtils.mappedSupplier(supplier, mapper);
        assertThrows(NullPointerException.class, () -> mappedSupplier.get());
    }

    @Test
    void mappedSupplier_withNonNullArguments_returnsMappedSupplier() {
        Supplier<String> supplier = () -> "123";
        Function<String, Integer> mapper = Integer::parseInt;
        Supplier<Integer> mappedSupplier = ConfigUtils.mappedSupplier(supplier, mapper);
        assertEquals(123, mappedSupplier.get());
    }
}
