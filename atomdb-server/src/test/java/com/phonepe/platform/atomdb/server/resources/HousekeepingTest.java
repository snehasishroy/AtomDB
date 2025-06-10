package com.phonepe.platform.atomdb.server.resources;

import com.phonepe.platform.atomdb.models.AtomDbResponse;
import com.phonepe.platform.atomdb.server.services.SampleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class HousekeepingTest {

    @Mock
    SampleService sampleService;

    private Housekeeping housekeeping;

    @BeforeEach
    void init() {
        MockitoAnnotations.initMocks(this);
        housekeeping = new Housekeeping(sampleService);
    }

    @Test
    @DisplayName("Should return 'prefix + message' string")
    void testSync() {
        String prefix = "Hello";
        String message = "JUnit5";
        AtomDbResponse<String> response = housekeeping.sync(prefix, message);
        assertEquals(String.format("%s %s", prefix, message), response.getResponse());
    }

    @Test
    @DisplayName("Should return default message when message query param is not provided")
    void testSyncDefaultMessage() {
        String prefix = "Hello";
        AtomDbResponse<String> response = housekeeping.sync(prefix, null);
        assertEquals(String.format("%s null", prefix), response.getResponse());
    }

    @Test
    @DisplayName("Should return sampleService result")
    void testSampleService() {
        String queryParam = "JUnit5";
        when(sampleService.sampleApi(anyString())).thenReturn(true);
        AtomDbResponse<Boolean> response = housekeeping.sampleService(queryParam);
        assertEquals(true, response.getResponse());
    }
}
