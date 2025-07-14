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

import org.eclipse.edc.connector.datamasking.rules.EmailMaskingStrategy;
import org.eclipse.edc.connector.datamasking.rules.NameMaskingStrategy;
import org.eclipse.edc.connector.datamasking.rules.PhoneNumberMaskingStrategy;
import org.eclipse.edc.connector.datamasking.spi.MaskingStrategy;
import org.eclipse.edc.spi.monitor.Monitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class DataMaskingServiceImplTest {

    private final Monitor monitor = mock(Monitor.class);
    private DataMaskingServiceImpl dataMaskingService;

    @BeforeEach
    void setUp() {
        var strategies = List.of(
                new NameMaskingStrategy(),
                new EmailMaskingStrategy(),
                new PhoneNumberMaskingStrategy()
        );
        dataMaskingService = new DataMaskingServiceImpl(monitor, true, new String[]{"name", "email", "phone"}, strategies);
    }

    @Test
    void shouldMaskName() {
        var jsonData = "{\"name\": \"John Smith\"}";
        var result = dataMaskingService.maskJsonData(jsonData);
        assertThat(result).contains("\"name\":\"J*** S****\"");
    }

    @Test
    void shouldMaskEmail() {
        var jsonData = "{\"email\": \"test@example.com\"}";
        var result = dataMaskingService.maskJsonData(jsonData);
        assertThat(result).contains("\"email\":\"t***@example.com\"");
    }

    @Test
    void shouldMaskPhone() {
        var jsonData = "{\"phone\": \"123-456-7890\"}";
        var result = dataMaskingService.maskJsonData(jsonData);
        assertThat(result).contains("\"phone\":\"***-***-*890\"");
    }

    @Test
    void shouldNotMaskUnconfiguredField() {
        var jsonData = "{\"address\": \"123 Main St\"}";
        var result = dataMaskingService.maskJsonData(jsonData);
        assertThat(result).contains("\"address\":\"123 Main St\"");
    }

    @Test
    void shouldReturnOriginalDataWhenMaskingIsDisabled() {
        List<MaskingStrategy> strategies = List.of(new NameMaskingStrategy());
        var disabledService = new DataMaskingServiceImpl(monitor, false, new String[]{"name"}, strategies);
        var jsonData = "{\"name\":\"John Smith\"}";
        var result = disabledService.maskJsonData(jsonData);
        assertThat(result).isEqualTo(jsonData);
    }

    @Test
    void shouldHandleNestedJson() {
        var jsonData = "{\"user\": {\"name\": \"Jane Doe\"}}";
        var result = dataMaskingService.maskJsonData(jsonData);
        assertThat(result).contains("\"name\":\"J*** D**\"");
    }

    @Test
    void shouldHandleArrayOfObjects() {
        var jsonData = "{\"users\":[{\"name\": \"John Smith\"},{\"name\": \"Jane Doe\"}]}";
        var result = dataMaskingService.maskJsonData(jsonData);
        assertThat(result).contains("\"name\":\"J*** S****\"");
        assertThat(result).contains("\"name\":\"J*** D**\"");
    }

    @Test
    void shouldHandleInvalidJson() {
        var invalidJsonData = "{\"name\": \"John Smith\""; // Missing closing brace
        var result = dataMaskingService.maskJsonData(invalidJsonData);
        assertThat(result).isEqualTo(invalidJsonData);
    }

    @Test
    void shouldHandleEmptyJsonObject() {
        var jsonData = "{}";
        var result = dataMaskingService.maskJsonData(jsonData);
        assertThat(result).isEqualTo(jsonData);
    }

    @Test
    void shouldHandleNullFieldValue() {
        var jsonData = "{\"name\": null}";
        var result = dataMaskingService.maskJsonData(jsonData);
        assertThat(result).contains("\"name\":null");
    }
}
