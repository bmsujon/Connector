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

public class EmailMaskingStrategy implements MaskingStrategy {

    @Override
    public boolean canMask(String fieldName) {
        return fieldName.toLowerCase().contains("email");
    }

    @Override
    public String mask(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return email; // Not a valid email to mask or nothing to mask
        }
        StringBuilder maskedEmail = new StringBuilder();
        maskedEmail.append(email.charAt(0));
        for (int i = 1; i < atIndex; i++) {
            maskedEmail.append('*');
        }
        maskedEmail.append(email.substring(atIndex));
        return maskedEmail.toString();
    }
}

