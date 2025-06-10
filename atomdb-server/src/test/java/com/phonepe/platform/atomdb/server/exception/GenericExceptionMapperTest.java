package com.phonepe.platform.atomdb.server.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GenericExceptionMapperTest {

    private GenericExceptionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new GenericExceptionMapper();
    }

    @Test
    void testToResponseWithWebApplicationException() {
        WebApplicationException exception = mock(WebApplicationException.class);
        Response response = Response.status(404).build();
        when(exception.getResponse()).thenReturn(response);

        Response result = mapper.toResponse(exception);

        assertEquals(404, result.getStatus());
        assertEquals("AtomDbResponse(success=false, response=null, errorCode=404, errorMessage=null)", result.getEntity().toString());
    }

    @Test
    void testToResponseWithException() {
        Exception exception = new RuntimeException("Something went wrong");

        Response result = mapper.toResponse(exception);

        assertEquals(500, result.getStatus());
        assertEquals("AtomDbResponse(success=false, response=null, errorCode=1000, errorMessage=Internal Server Error)", result.getEntity().toString());
    }
}
