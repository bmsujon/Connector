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

package org.eclipse.edc.connector.datamasking.rules;

import org.eclipse.edc.connector.datamasking.spi.MaskingStrategy;

import java.util.regex.Pattern;

public class NameMaskingStrategy implements MaskingStrategy {

    private static final Pattern NAME_SPLIT_PATTERN = Pattern.compile("\\s+");

    @Override
    public boolean canMask(String fieldName) {
        return fieldName.toLowerCase().contains("name");
    }

    @Override
    public String mask(String name) {
        if (name == null || name.trim().isEmpty()) {
            return name;
        }
        String[] parts = NAME_SPLIT_PATTERN.split(name.trim());
        StringBuilder maskedName = new StringBuilder();
        for (String part : parts) {
            if (part.length() > 0) {
                maskedName.append(part.charAt(0));
                for (int i = 1; i < part.length(); i++) {
                    maskedName.append('*');
                }
                maskedName.append(" ");
            }
        }
        return maskedName.toString().trim();
    }
}
