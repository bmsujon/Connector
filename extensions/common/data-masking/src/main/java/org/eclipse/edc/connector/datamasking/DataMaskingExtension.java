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

import org.eclipse.edc.connector.datamasking.DataMaskingServiceImpl;
import org.eclipse.edc.connector.datamasking.DataMaskingTransformer;
import org.eclipse.edc.connector.datamasking.rules.EmailMaskingStrategy;
import org.eclipse.edc.connector.datamasking.rules.NameMaskingStrategy;
import org.eclipse.edc.connector.datamasking.rules.PhoneNumberMaskingStrategy;
import org.eclipse.edc.connector.datamasking.spi.DataMaskingService;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.runtime.metamodel.annotation.Provides;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.transform.spi.TypeTransformerRegistry;

import java.util.List;

/**
 * Extension that provides data masking capabilities for sensitive fields
 * in data exchange flows. This extension integrates with EDC's transformation
 * pipeline to automatically mask configured sensitive data fields.
 */
@Provides(DataMaskingService.class)
public class DataMaskingExtension implements ServiceExtension {

    public static final String NAME = "Data Masking Extension";

    @Inject
    private TypeTransformerRegistry transformerRegistry;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var monitor = context.getMonitor();
        var configuration = new DataMaskingConfiguration(context.getConfig());

        // Parse the fields to mask
        String[] fields = new String[0];
        var fieldsToMask = configuration.getFieldsToMask();
        if (fieldsToMask != null && !fieldsToMask.trim().isEmpty()) {
            fields = fieldsToMask.split(",");
        }

        var strategies = List.of(
                new NameMaskingStrategy(),
                new EmailMaskingStrategy(),
                new PhoneNumberMaskingStrategy()
        );

        var dataMaskingService = new DataMaskingServiceImpl(monitor, configuration.isMaskingEnabled(), fields, strategies);
        context.registerService(DataMaskingService.class, dataMaskingService);

        // Register the data masking transformer if masking is enabled
        if (configuration.isMaskingEnabled()) {
            var transformer = new DataMaskingTransformer(dataMaskingService, monitor);
            transformerRegistry.register(transformer);
            monitor.info("Data Masking Transformer registered");
        }

        monitor.info("Data Masking Extension initialized. Masking enabled: " + configuration.isMaskingEnabled() +
                ", Fields: " + (fields.length > 0 ? String.join(", ", fields) : "default"));
    }

    @Provider
    public DataMaskingService dataMaskingService(ServiceExtensionContext context) {
        return context.getService(DataMaskingService.class);
    }
}
