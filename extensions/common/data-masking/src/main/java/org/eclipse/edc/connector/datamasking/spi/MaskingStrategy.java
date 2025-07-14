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
 *       Eclipse EDC Contributors - initial implementation
 *
 */

package org.eclipse.edc.connector.datamasking.spi;

/**
 * Defines a strategy for masking a specific type of data.
 */
public interface MaskingStrategy {

    /**
     * Checks if this strategy can be applied to the given field.
     *
     * @param fieldName the name of the field to check.
     * @return true if this strategy can mask the field, false otherwise.
     */
    boolean canMask(String fieldName);

    /**
     * Masks the given value.
     *
     * @param value the value to mask.
     * @return the masked value.
     */
    String mask(String value);
}

