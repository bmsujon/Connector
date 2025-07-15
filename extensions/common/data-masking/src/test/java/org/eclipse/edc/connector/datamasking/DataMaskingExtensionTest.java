/*
 *  Copyright (c) 2025 Eclipse EDC Contributors
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Eclipse EDC Contributors - Data Masking Extension
 *
 */

package org.eclipse.edc.connector.datamasking;

import org.eclipse.edc.connector.datamasking.spi.DataMaskingService;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.system.configuration.Config;
import org.eclipse.edc.transform.spi.TypeTransformerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DataMaskingExtensionTest {

    private final ServiceExtensionContext context = mock(ServiceExtensionContext.class);
    private final TypeTransformerRegistry transformerRegistry = mock(TypeTransformerRegistry.class);
    private final Config config = mock(Config.class);
    private final Monitor monitor = mock(Monitor.class);
    private DataMaskingExtension extension;

    @BeforeEach
    void setUp() throws Exception {
        extension = new DataMaskingExtension();
        when(context.getMonitor()).thenReturn(monitor);
        when(context.getService(TypeTransformerRegistry.class)).thenReturn(transformerRegistry);
        when(context.getConfig()).thenReturn(config);

        // Manually inject the mock transformerRegistry
        Field transformerRegistryField = DataMaskingExtension.class.getDeclaredField("transformerRegistry");
        transformerRegistryField.setAccessible(true);
        transformerRegistryField.set(extension, transformerRegistry);
    }

    @Test
    void shouldReturnCorrectName() {
        assertThat(extension.name()).isEqualTo("Data Masking Extension");
    }

    @Test
    void shouldInitializeWithDefaultSettings() {
        when(config.getBoolean("edc.data.masking.enabled", true)).thenReturn(true);
        when(config.getString("edc.data.masking.fields", null)).thenReturn(null);
        var serviceCaptor = ArgumentCaptor.forClass(DataMaskingService.class);

        extension.initialize(context);

        verify(context).registerService(eq(DataMaskingService.class), serviceCaptor.capture());
        verify(transformerRegistry).register(any(DataMaskingTransformer.class));

        var service = serviceCaptor.getValue();
        assertThat(service).isInstanceOf(DataMaskingServiceImpl.class);
        // Further tests could verify the registered strategies if needed
    }

    @Test
    void shouldInitializeWithCustomFields() {
        when(config.getBoolean("edc.data.masking.enabled", true)).thenReturn(true);
        when(config.getString("edc.data.masking.fields", null)).thenReturn("name,custom");
        var serviceCaptor = ArgumentCaptor.forClass(DataMaskingService.class);

        extension.initialize(context);

        verify(context).registerService(eq(DataMaskingService.class), serviceCaptor.capture());
        verify(transformerRegistry).register(any(DataMaskingTransformer.class));
        assertThat(serviceCaptor.getValue()).isNotNull();
    }

    @Test
    void shouldNotRegisterTransformerWhenDisabled() {
        when(config.getBoolean("edc.data.masking.enabled", true)).thenReturn(false);

        extension.initialize(context);

        verify(context).registerService(eq(DataMaskingService.class), any(DataMaskingServiceImpl.class));
        verify(transformerRegistry, org.mockito.Mockito.never()).register(any(DataMaskingTransformer.class));
    }

    @Test
    void shouldInitializeWithEmptyFields() {
        when(config.getBoolean("edc.data.masking.enabled", true)).thenReturn(true);
        when(config.getString("edc.data.masking.fields", null)).thenReturn("");
        var serviceCaptor = ArgumentCaptor.forClass(DataMaskingService.class);

        extension.initialize(context);

        verify(context).registerService(eq(DataMaskingService.class), serviceCaptor.capture());
        verify(transformerRegistry).register(any(DataMaskingTransformer.class));
        assertThat(serviceCaptor.getValue()).isNotNull();
    }
}
