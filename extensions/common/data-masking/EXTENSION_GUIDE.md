# Adding New Sensitive Data Types - Extension Guide

This guide demonstrates how to extend the Data Masking Extension to support new sensitive data types like bank accounts, social security numbers, or credit cards.

## Overview

The Data Masking Extension is designed with extensibility in mind, following the Strategy Pattern. Adding a new masking rule is a straightforward process that involves creating a new `MaskingStrategy` implementation and registering it with the extension.

## Step 1: Create a New MaskingStrategy Implementation

First, create a new Java class that implements the `org.eclipse.edc.connector.datamasking.spi.MaskingStrategy` interface. This interface has two methods:

- `canMask(String fieldName)`: This method determines if the strategy should be applied to a given field. You can use any logic here, such as checking for specific field names or patterns.
- `mask(String value)`: This method contains the logic for masking the value of the field.

### Example: `CreditCardMaskingStrategy`

Let's create a strategy to mask credit card numbers, leaving only the last four digits visible.

```java
package org.eclipse.edc.connector.datamasking.rules;

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

## Step 2: Register the New Strategy

Next, open the `DataMaskingExtension.java` file and add your new strategy to the list of strategies that are passed to the `DataMaskingServiceImpl`.

```java
// File: DataMaskingExtension.java

// ... imports
import org.eclipse.edc.connector.datamasking.rules.CreditCardMaskingStrategy; // Import your new strategy

// ... class definition

    @Override
    public void initialize(ServiceExtensionContext context) {
        // ... existing code

        var strategies = List.of(
                new NameMaskingStrategy(),
                new EmailMaskingStrategy(),
                new PhoneNumberMaskingStrategy(),
                new CreditCardMaskingStrategy() // Add your new strategy to the list
        );

        var dataMaskingService = new DataMaskingServiceImpl(monitor, configuration.isMaskingEnabled(), fields, strategies);
        // ... existing code
    }
```

## Step 3: Configure the New Field (Optional)

If you want to mask a field that is not covered by the default field names, add it to your configuration file.

```properties
# config.properties
edc.data.masking.fields=name,phone,email,creditCard,cc_number
```

If your `canMask` method is robust enough (like the example above), you may not need to explicitly configure every field name. The default field list will catch common variations, but this provides an override.

That's it! Your new masking rule is now integrated into the data masking extension and will be automatically applied during data transfers.
