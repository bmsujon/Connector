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
import org.eclipse.edc.transform.spi.TransformerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DataMaskingTransformerTest {

    private final DataMaskingService dataMaskingService = mock(DataMaskingService.class);
    private final Monitor monitor = mock(Monitor.class);
    private final TransformerContext context = mock(TransformerContext.class);
    private DataMaskingTransformer transformer;

    @BeforeEach
    void setUp() {
        transformer = new DataMaskingTransformer(dataMaskingService, monitor);
    }

    @Test
    void shouldMaskData() throws IOException {
        var jsonData = "{\"name\": \"John Doe\"}";
        var inputStream = new ByteArrayInputStream(jsonData.getBytes(StandardCharsets.UTF_8));
        when(dataMaskingService.maskJsonData(anyString())).thenReturn("{\"name\": \"J*** D**\"}");

        var resultStream = transformer.transform(inputStream, context);

        assertThat(resultStream).isNotNull();
        var resultString = new String(resultStream.readAllBytes(), StandardCharsets.UTF_8);
        assertThat(resultString).isEqualTo("{\"name\": \"J*** D**\"}");
    }

    @Test
    void shouldReturnNullOnTransformFailure() {
        var jsonData = "{\"name\": \"John Doe\"}";
        var inputStream = new ByteArrayInputStream(jsonData.getBytes(StandardCharsets.UTF_8));
        when(dataMaskingService.maskJsonData(anyString())).thenThrow(new RuntimeException("Test Exception"));

        var resultStream = transformer.transform(inputStream, context);

        assertThat(resultStream).isNull();
    }

    @Test
    void shouldReturnNullOnIOException() throws IOException {
        var inputStream = mock(InputStream.class);
        when(inputStream.transferTo(any())).thenThrow(new IOException("Test IO Exception"));

        var resultStream = transformer.transform(inputStream, context);

        assertThat(resultStream).isNull();
        verify(context).reportProblem(anyString());
    }

    @Test
    void shouldHandleEmptyInputStream() throws IOException {
        var inputStream = new ByteArrayInputStream(new byte[0]);
        when(dataMaskingService.maskJsonData("")).thenReturn("");

        var resultStream = transformer.transform(inputStream, context);

        assertThat(resultStream).isNotNull();
        var resultString = new String(resultStream.readAllBytes(), StandardCharsets.UTF_8);
        assertThat(resultString).isEmpty();
    }
}
