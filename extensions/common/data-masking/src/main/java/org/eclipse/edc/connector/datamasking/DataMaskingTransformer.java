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
import org.eclipse.edc.transform.spi.TypeTransformer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * A type transformer that applies data masking to string-based data.
 * It intercepts data of type String and applies masking rules before
 * passing it on.
 */
public class DataMaskingTransformer implements TypeTransformer<InputStream, InputStream> {

    private final DataMaskingService dataMaskingService;
    private final Monitor monitor;

    public DataMaskingTransformer(DataMaskingService dataMaskingService, Monitor monitor) {
        this.dataMaskingService = dataMaskingService;
        this.monitor = monitor;
    }

    @Override
    public Class<InputStream> getInputType() {
        return InputStream.class;
    }

    @Override
    public Class<InputStream> getOutputType() {
        return InputStream.class;
    }

    @Override
    public @Nullable InputStream transform(@NotNull InputStream data, @NotNull TransformerContext context) {
        try {
            // Read the input stream into a string
            var baos = new ByteArrayOutputStream();
            data.transferTo(baos);
            var jsonData = baos.toString(StandardCharsets.UTF_8);

            // Mask the data
            var maskedData = dataMaskingService.maskJsonData(jsonData);

            // Return the masked data as a new input stream
            return new ByteArrayInputStream(maskedData.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            monitor.severe("Failed to read input stream for data masking", e);
            context.reportProblem("Failed to read input stream: " + e.getMessage());
            return null; // Returning null indicates a transformation failure
        } catch (Exception e) {
            monitor.severe("Failed to apply data masking transformation", e);
            context.reportProblem("Data masking failed: " + e.getMessage());
            return null; // Returning null indicates a transformation failure
        }
    }
}
