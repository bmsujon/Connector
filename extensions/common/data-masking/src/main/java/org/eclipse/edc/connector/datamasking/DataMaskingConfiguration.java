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

import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.system.configuration.Config;

/**
 * Configuration class for the Data Masking extension.
 * This class holds all configurable settings for the data masking functionality.
 */
public class DataMaskingConfiguration {

    private static final String MASKING_ENABLED_KEY = "edc.data.masking.enabled";
    private static final String FIELDS_TO_MASK_KEY = "edc.data.masking.fields";
    private static final boolean MASKING_ENABLED_DEFAULT = true;

    private boolean maskingEnabled;
    private String fieldsToMask;

    public DataMaskingConfiguration(Config config) {
        this.maskingEnabled = config.getBoolean(MASKING_ENABLED_KEY, MASKING_ENABLED_DEFAULT);
        this.fieldsToMask = config.getString(FIELDS_TO_MASK_KEY, null);
    }

    /**
     * Checks if data masking is enabled.
     *
     * @return true if data masking is enabled, false otherwise
     */
    public boolean isMaskingEnabled() {
        return maskingEnabled;
    }

    /**
     * Gets the comma-separated list of fields to mask.
     *
     * @return the fields to mask
     */
    public String getFieldsToMask() {
        return fieldsToMask;
    }
}
