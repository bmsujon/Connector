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

public class PhoneNumberMaskingStrategy implements MaskingStrategy {

    @Override
    public boolean canMask(String fieldName) {
        return fieldName.toLowerCase().contains("phone");
    }

    @Override
    public String mask(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() <= 3) {
            return phoneNumber;
        }
        int length = phoneNumber.length();
        StringBuilder maskedNumber = new StringBuilder();
        for (int i = 0; i < length - 3; i++) {
            char c = phoneNumber.charAt(i);
            if (Character.isDigit(c)) {
                maskedNumber.append('*');
            } else {
                maskedNumber.append(c);
            }
        }
        maskedNumber.append(phoneNumber.substring(length - 3));
        return maskedNumber.toString();
    }
}

