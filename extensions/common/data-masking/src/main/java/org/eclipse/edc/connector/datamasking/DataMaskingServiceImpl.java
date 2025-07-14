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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.eclipse.edc.connector.datamasking.spi.DataMaskingService;
import org.eclipse.edc.connector.datamasking.spi.MaskingStrategy;
import org.eclipse.edc.spi.monitor.Monitor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service that masks sensitive data fields in JSON objects based on configured rules.
 */
public class DataMaskingServiceImpl implements DataMaskingService {

    private static final Set<String> DEFAULT_FIELDS_TO_MASK = new HashSet<>(Arrays.asList(
            "name", "phone", "phonenumber", "phone_number", "email", "emailaddress", "email_address"
    ));

    private final Monitor monitor;
    private final boolean maskingEnabled;
    private final Set<String> fieldsToMask;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<MaskingStrategy> maskingStrategies;

    public DataMaskingServiceImpl(Monitor monitor, boolean maskingEnabled, String[] fieldsToMask, List<MaskingStrategy> maskingStrategies) {
        this.monitor = monitor;
        this.maskingEnabled = maskingEnabled;
        this.maskingStrategies = maskingStrategies;
        var fieldsToMaskSet = new HashSet<String>();
        if (fieldsToMask != null && fieldsToMask.length > 0) {
            for (String field : fieldsToMask) {
                fieldsToMaskSet.add(field.trim().toLowerCase());
            }
            this.fieldsToMask = fieldsToMaskSet;
        } else {
            this.fieldsToMask = DEFAULT_FIELDS_TO_MASK;
        }
    }

    @Override
    public String maskName(String name) {
        return findMaskingStrategy("name").map(s -> s.mask(name)).orElse(name);
    }

    @Override
    public String maskPhoneNumber(String phoneNumber) {
        return findMaskingStrategy("phone").map(s -> s.mask(phoneNumber)).orElse(phoneNumber);
    }

    @Override
    public String maskEmail(String email) {
        return findMaskingStrategy("email").map(s -> s.mask(email)).orElse(email);
    }

    @Override
    public String maskJsonData(String jsonObject) {
        if (!maskingEnabled || jsonObject == null || jsonObject.trim().isEmpty()) {
            return jsonObject;
        }

        try {
            JsonNode rootNode = objectMapper.readTree(jsonObject);
            if (rootNode.isObject()) {
                traverseAndMask((ObjectNode) rootNode);
                return objectMapper.writeValueAsString(rootNode);
            }
            // If root is not an object (e.g., an array), return original data as no masking is applied.
            return jsonObject;
        } catch (Exception e) {
            monitor.severe("Data masking failed: " + e.getMessage(), e);
            return jsonObject; // Return original data on failure
        }
    }

    @Override
    public boolean isMaskingEnabledForField(String fieldName) {
        return maskingEnabled && fieldsToMask.contains(fieldName.toLowerCase());
    }

    private void traverseAndMask(ObjectNode node) {
        node.fieldNames().forEachRemaining(fieldName -> {
            JsonNode childNode = node.get(fieldName);
            if (isMaskingEnabledForField(fieldName)) {
                findMaskingStrategy(fieldName).ifPresent(strategy -> {
                    if (childNode.isTextual()) {
                        String maskedValue = strategy.mask(childNode.asText());
                        node.put(fieldName, maskedValue);
                    }
                });
            }

            if (childNode.isObject()) {
                traverseAndMask((ObjectNode) childNode);
            } else if (childNode.isArray()) {
                childNode.forEach(element -> {
                    if (element.isObject()) {
                        traverseAndMask((ObjectNode) element);
                    }
                });
            }
        });
    }

    private Optional<MaskingStrategy> findMaskingStrategy(String fieldName) {
        return maskingStrategies.stream()
                .filter(strategy -> strategy.canMask(fieldName))
                .findFirst();
    }
}
