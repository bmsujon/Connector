# Adding New Sensitive Data Types - Extension Guide

This guide demonstrates how to extend the Data Masking Extension to support new sensitive data types like bank accounts, social security numbers, or credit cards.

## Overview

The Data Masking Extension is designed with extensibility in mind. It uses a registry pattern where the `DataMaskingService` acts as a registry for different `MaskingStrategy` implementations. To add a new masking rule, you simply need to create your own `MaskingStrategy` and register it with the service.

This is typically done by creating a separate EDC extension that depends on the `data-masking` extension.

## Step 1: Create a New `MaskingStrategy` Implementation

First, create a new Java class that implements the `org.eclipse.edc.connector.datamasking.spi.MaskingStrategy` interface. This interface has two methods:

- `canMask(String fieldName)`: This method determines if the strategy should be applied to a given field. You can use any logic here, such as checking for specific field names or patterns.
- `mask(String value)`: This method contains the logic for masking the value of the field.

### Example: `CreditCardMaskingStrategy`

Let's create a strategy to mask credit card numbers, leaving only the last four digits visible.

```java
package com.example.edc.extension.masking;

import org.eclipse.edc.connector.datamasking.spi.MaskingStrategy;

public class CreditCardMaskingStrategy implements MaskingStrategy {

    @Override
    public boolean canMask(String fieldName) {
        String lowerCaseFieldName = fieldName.toLowerCase();
        return lowerCaseFieldName.contains("creditcard") || lowerCaseFieldName.contains("cc_number");
    }

    @Override
    public String mask(String value) {
        if (value == null || value.length() <= 4) {
            return value;
        }
        int length = value.length();
        StringBuilder maskedValue = new StringBuilder();
        for (int i = 0; i < length - 4; i++) {
            char c = value.charAt(i);
            if (Character.isDigit(c)) {
                maskedValue.append('*');
            } else {
                maskedValue.append(c);
            }
        }
        maskedValue.append(value.substring(length - 4));
        return maskedValue.toString();
    }
}
```

## Step 2: Create a New Extension to Register the Strategy

Create a new EDC `ServiceExtension` that will register your custom strategy. This extension will get the `DataMaskingService` from the context and use its `register` method.

```java
package com.example.edc.extension.masking;

import org.eclipse.edc.connector.datamasking.spi.DataMaskingService;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

public class CustomMaskingExtension implements ServiceExtension {

    @Inject
    private DataMaskingService dataMaskingService;

    @Override
    public void initialize(ServiceExtensionContext context) {
        dataMaskingService.register(new CreditCardMaskingStrategy());
    }
}
```

## Step 3: Register the Custom Extension as a Service

For the EDC runtime to discover your new `CustomMaskingExtension`, you must create a service provider configuration file.

1.  In your custom extension's resources directory, create the following folder structure: `src/main/resources/META-INF/services`.
2.  Inside the `services` directory, create a file named `org.eclipse.edc.spi.system.ServiceExtension`.
3.  Add the fully qualified name of your new extension class to this file:

    ```
    com.example.edc.extension.masking.CustomMaskingExtension
    ```

This file tells the Java ServiceLoader that your class is an available `ServiceExtension`, allowing the EDC runtime to load and initialize it.

## Step 4: Include Your New Extension in the Runtime

Finally, add your new extension module as a dependency to your EDC runtime launcher. This will ensure that your custom extension is loaded and the new masking strategy is registered with the `DataMaskingService`.

```kotlin
// In your launcher's build.gradle.kts
dependencies {
    // ... other dependencies
    implementation(project(":extensions:common:data-masking"))
    implementation(project(":extensions:custom:custom-masking-extension")) // Your new extension
}
```

## Step 5: Configure the New Field (Optional)

If you want to mask a field that is not covered by the default field names, add it to your `config.properties` file.

```properties
# config.properties
edc.data.masking.fields=name,phone,email,creditCard,cc_number
```

## Step 6: Test Your New Strategy (Recommended)

It is highly recommended to write unit tests for your new `MaskingStrategy` to ensure it behaves as expected.

### Example: `CreditCardMaskingStrategyTest.java`

```java
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class CreditCardMaskingStrategyTest {

    private final CreditCardMaskingStrategy strategy = new CreditCardMaskingStrategy();

    @Test
    void shouldMaskCreditCardNumber() {
        assertThat(strategy.mask("4111-1111-1111-1111")).isEqualTo("****-****-****-1111");
    }

    @Test
    void shouldNotMaskShortValues() {
        assertThat(strategy.mask("1234")).isEqualTo("1234");
    }
}
```

That's it! Your new masking rule is now fully integrated into the data masking extension and will be automatically applied during data transfers.
