package com.phonepe.platform.atomdb.server.services.impl;

import com.phonepe.platform.atomdb.server.AtomDbConfiguration;
import com.phonepe.platform.atomdb.server.events.EventIngestor;
import com.phonepe.platform.atomdb.server.guice.bindings.BindingNames;
import com.phonepe.platform.http.v2.common.HttpConfiguration;
import com.phonepe.platform.http.v2.executor.HttpGetExecutor;
import com.phonepe.platform.http.v2.executor.factory.HttpExecutorBuilderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.vyarus.dropwizard.guice.module.yaml.bind.Config;

import javax.inject.Named;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class SampleServiceHttpImplTest {

    @Mock
    private EventIngestor mockEventIngestor;

    @Mock
    @Config(AtomDbConfiguration.Fields.SAMPLE_APP_CONFIGURATION)
    private HttpConfiguration mockClientConfiguration;

    @Mock
    @Named(BindingNames.SAMPLE_SERVICE)
    private HttpExecutorBuilderFactory mockExecutorFactory;

    @Mock
    private HttpGetExecutor httpGetExecutor;

    private SampleServiceHttpImpl sampleServiceHttpImpl;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        sampleServiceHttpImpl = new SampleServiceHttpImpl(
                mockEventIngestor,
                mockClientConfiguration,
                mockExecutorFactory
        );
    }

    @Test
    void testSampleApiReturnsTrueWhenHttpGetSucceeds() {
        // Given
        HttpGetExecutor.HttpGetExecutorBuilder mockBuilder = mock(HttpGetExecutor.HttpGetExecutorBuilder.class);
        when(mockBuilder.url(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.responseType(Boolean.class)).thenReturn(mockBuilder);
        when(mockBuilder.command(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.nonSuccessResponseConsumer(any())).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(httpGetExecutor);
        when(httpGetExecutor.executeTracked()).thenReturn(true);
        when(mockExecutorFactory.httpGetExecutorBuilder()).thenReturn(mockBuilder);

        // When
        boolean result = sampleServiceHttpImpl.sampleApi("test");

        // Then
        assertTrue(result);
        verify(mockBuilder).url("/v1/some/path?queryParam=test");
        verify(mockBuilder).responseType(Boolean.class);
        verify(mockBuilder).command("sampleApi");
        verify(mockBuilder).nonSuccessResponseConsumer(any());
        verify(mockBuilder).build();
        verify(httpGetExecutor).executeTracked();
        verifyNoMoreInteractions(mockBuilder);
        verifyNoInteractions(mockEventIngestor);
    }

    @Test
    void testSampleApiReturnsFalseWhenHttpGetFails() {
        // Given
        HttpGetExecutor.HttpGetExecutorBuilder mockBuilder = mock(HttpGetExecutor.HttpGetExecutorBuilder.class);
        when(mockBuilder.url(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.responseType(Boolean.class)).thenReturn(mockBuilder);
        when(mockBuilder.command(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.nonSuccessResponseConsumer(any())).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(httpGetExecutor);
        when(httpGetExecutor.executeTracked()).thenReturn(false);
        when(mockExecutorFactory.httpGetExecutorBuilder()).thenReturn(mockBuilder);

        // When
        boolean result = sampleServiceHttpImpl.sampleApi("test");

        // Then
        assertFalse(result);
        verify(mockBuilder).url("/v1/some/path?queryParam=test");
        verify(mockBuilder).responseType(Boolean.class);
        verify(mockBuilder).command("sampleApi");

        verify(mockBuilder).nonSuccessResponseConsumer(any());
        verify(mockBuilder).build();
        verify(httpGetExecutor).executeTracked();
        verifyNoMoreInteractions(mockBuilder);
        verifyNoMoreInteractions(mockEventIngestor);
    }
}
