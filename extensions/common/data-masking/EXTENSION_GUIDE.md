# Adding New Sensitive Data Types - Extension Guide

This guide demonstrates how to extend the Data Masking Extension to support new sensitive data types like bank accounts, social security numbers, credit cards, etc.

## Overview

The Data Masking Extension is designed with extensibility in mind, following SOLID principles. Adding new sensitive data types can be accomplished through multiple approaches, depending on your requirements.

## Approach 1: Configuration-Only Extension (Recommended for Simple Cases)

### 🎯 **Zero Code Changes Required**

For basic masking needs, simply add the new field names to your configuration:

```properties
# config.properties
edc.data.masking.enabled=true
edc.data.masking.fields=name,phone,email,bankAccount,accountNumber,iban,ssn,creditCard
```

### How It Works

The existing system will automatically:

1. Detect the configured field names (case-insensitive)
2. Apply the default masking strategy (preserve first character + asterisks)
3. Handle multiple field name variations

### Example Results

```json
// Input
{
  "bankAccount": "1234567890123456",
  "ssn": "123-45-6789",
  "creditCard": "4111111111111111"
}

// Output (default masking)
{
  "bankAccount": "1***************",
  "ssn": "1**********",
  "creditCard": "4***************"
}
```

### Pros & Cons

✅ **Pros:**

- No code changes required
- Zero development time
- Immediate deployment
- No testing overhead

❌ **Cons:**

- Basic masking only (first character + asterisks)
- No specialized masking patterns
- Limited customization

---

## Approach 2: Enhanced Implementation (Recommended for Sophisticated Masking)

### 🎯 **Minimal Code Changes for Custom Masking Logic**

For specialized masking patterns (e.g., preserve last 4 digits of bank accounts), extend the implementation.

### Step 1: Extend the SPI Interface

```java
// File: DataMaskingService.java
// Add new method to the interface

/**
 * Masks a bank account number by keeping only the last 4 digits visible.
 * Example: "1234567890123456" -> "************3456"
 *
 * @param bankAccount the bank account number to mask
 * @return the masked bank account number
 */
String maskBankAccount(String bankAccount);

/**
 * Masks a social security number by keeping only the last 4 digits visible.
 * Example: "123-45-6789" -> "***-**-6789"
 *
 * @param ssn the social security number to mask
 * @return the masked SSN
 */
String maskSocialSecurityNumber(String ssn);

/**
 * Masks a credit card number by keeping only the last 4 digits visible.
 * Example: "4111-1111-1111-1111" -> "****-****-****-1111"
 *
 * @param creditCard the credit card number to mask
 * @return the masked credit card number
 */
String maskCreditCard(String creditCard);
```

### Step 2: Implement in DataMaskingServiceImpl

```java
// File: DataMaskingServiceImpl.java
// Add implementation methods

@Override
public String maskBankAccount(String bankAccount) {
    if (bankAccount == null || bankAccount.trim().isEmpty()) {
        return bankAccount;
    }

    String trimmed = bankAccount.trim();
    String digitsOnly = trimmed.replaceAll("[^0-9]", "");

    if (digitsOnly.length() <= 4) {
        return MASK_CHARACTER.repeat(trimmed.length());
    }

    String lastFour = digitsOnly.substring(digitsOnly.length() - 4);
    StringBuilder masked = new StringBuilder(trimmed);

    // Replace digits with asterisks, keeping last 4
    int digitsFound = 0;
    for (int i = trimmed.length() - 1; i >= 0; i--) {
        if (Character.isDigit(trimmed.charAt(i))) {
            digitsFound++;
            if (digitsFound > 4) {
                masked.setCharAt(i, MASK_CHARACTER.charAt(0));
            }
        }
    }

    return masked.toString();
}

@Override
public String maskSocialSecurityNumber(String ssn) {
    if (ssn == null || ssn.trim().isEmpty()) {
        return ssn;
    }

    String trimmed = ssn.trim();
    String digitsOnly = trimmed.replaceAll("[^0-9]", "");

    if (digitsOnly.length() < 4) {
        return MASK_CHARACTER.repeat(trimmed.length());
    }

    StringBuilder masked = new StringBuilder(trimmed);
    int digitsFound = 0;

    // Keep last 4 digits, mask the rest
    for (int i = trimmed.length() - 1; i >= 0; i--) {
        if (Character.isDigit(trimmed.charAt(i))) {
            digitsFound++;
            if (digitsFound > 4) {
                masked.setCharAt(i, MASK_CHARACTER.charAt(0));
            }
        }
    }

    return masked.toString();
}

@Override
public String maskCreditCard(String creditCard) {
    if (creditCard == null || creditCard.trim().isEmpty()) {
        return creditCard;
    }

    String trimmed = creditCard.trim();
    String digitsOnly = trimmed.replaceAll("[^0-9]", "");

    if (digitsOnly.length() < 4) {
        return MASK_CHARACTER.repeat(trimmed.length());
    }

    StringBuilder masked = new StringBuilder(trimmed);
    int digitsFound = 0;

    // Keep last 4 digits, mask the rest
    for (int i = trimmed.length() - 1; i >= 0; i--) {
        if (Character.isDigit(trimmed.charAt(i))) {
            digitsFound++;
            if (digitsFound > 4) {
                masked.setCharAt(i, MASK_CHARACTER.charAt(0));
            }
        }
    }

    return masked.toString();
}
```

### Step 3: Update Field Detection Logic

```java
// File: DataMaskingServiceImpl.java
// Modify the maskFieldValue() method

private String maskFieldValue(String fieldName, String value) {
    String lowerFieldName = fieldName.toLowerCase();

    if ("name".equals(lowerFieldName)) {
        return maskName(value);
    } else if ("phone".equals(lowerFieldName) || "phonenumber".equals(lowerFieldName) ||
            "phone_number".equals(lowerFieldName)) {
        return maskPhoneNumber(value);
    } else if ("email".equals(lowerFieldName) || "emailaddress".equals(lowerFieldName) ||
            "email_address".equals(lowerFieldName)) {
        return maskEmail(value);
    } else if ("bankaccount".equals(lowerFieldName) || "accountnumber".equals(lowerFieldName) ||
            "account_number".equals(lowerFieldName) || "iban".equals(lowerFieldName)) {
        return maskBankAccount(value);
    } else if ("ssn".equals(lowerFieldName) || "socialsecuritynumber".equals(lowerFieldName) ||
            "social_security_number".equals(lowerFieldName)) {
        return maskSocialSecurityNumber(value);
    } else if ("creditcard".equals(lowerFieldName) || "credit_card".equals(lowerFieldName) ||
            "cardnumber".equals(lowerFieldName) || "card_number".equals(lowerFieldName)) {
        return maskCreditCard(value);
    }

    // Default masking strategy - keep first character
    if (value.length() <= 1) {
        return value;
    }
    return value.charAt(0) + MASK_CHARACTER.repeat(value.length() - 1);
}
```

### Step 4: Add Comprehensive Tests

```java
// File: DataMaskingServiceImplTest.java
// Add test methods for new functionality

@Test
void shouldMaskBankAccount_standardFormat() {
    // given
    String bankAccount = "1234567890123456";

    // when
    String maskedAccount = dataMaskingService.maskBankAccount(bankAccount);

    // then
    assertThat(maskedAccount).isEqualTo("************3456");
}

@Test
void shouldMaskBankAccount_withDashes() {
    // given
    String bankAccount = "1234-5678-9012-3456";

    // when
    String maskedAccount = dataMaskingService.maskBankAccount(bankAccount);

    // then
    assertThat(maskedAccount).isEqualTo("****-****-****-3456");
}

@Test
void shouldMaskSSN_standardFormat() {
    // given
    String ssn = "123-45-6789";

    // when
    String maskedSSN = dataMaskingService.maskSocialSecurityNumber(ssn);

    // then
    assertThat(maskedSSN).isEqualTo("***-**-6789");
}

@Test
void shouldMaskCreditCard_visaFormat() {
    // given
    String creditCard = "4111-1111-1111-1111";

    // when
    String maskedCard = dataMaskingService.maskCreditCard(creditCard);

    // then
    assertThat(maskedCard).isEqualTo("****-****-****-1111");
}

@Test
void shouldMaskNewFieldsInJSON() {
    // given
    String inputJson = """
        {
            "name": "John Doe",
            "bankAccount": "1234567890123456",
            "ssn": "123-45-6789",
            "creditCard": "4111-1111-1111-1111"
        }
        """;

    // when
    String maskedJson = dataMaskingService.maskJsonData(inputJson);

    // then
    assertThat(maskedJson).contains("\"name\":\"J*** D**\"");
    assertThat(maskedJson).contains("\"bankAccount\":\"************3456\"");
    assertThat(maskedJson).contains("\"ssn\":\"***-**-6789\"");
    assertThat(maskedJson).contains("\"creditCard\":\"****-****-****-1111\"");
}
```

### Step 5: Update Configuration

```properties
# config.properties
edc.data.masking.enabled=true
edc.data.masking.fields=name,phone,email,bankAccount,ssn,creditCard
```

### Example Results

```json
// Input
{
  "customer": {
    "name": "John Doe",
    "email": "john@example.com",
    "bankAccount": "1234567890123456",
    "ssn": "123-45-6789",
    "creditCard": "4111-1111-1111-1111"
  }
}

// Output (enhanced masking)
{
  "customer": {
    "name": "J*** D**",
    "email": "j***@example.com",
    "bankAccount": "************3456",
    "ssn": "***-**-6789",
    "creditCard": "****-****-****-1111"
  }
}
```

---

## Approach 3: Plugin Architecture (Advanced)

### 🎯 **For Multiple Custom Masking Providers**

Create a plugin-based system for complex scenarios:

```java
// Create a masking provider interface
public interface MaskingProvider {
    boolean canMask(String fieldName);
    String mask(String value);
    int getPriority(); // For ordering providers
}

// Implement specific providers
public class FinancialDataMaskingProvider implements MaskingProvider {
    @Override
    public boolean canMask(String fieldName) {
        return fieldName.toLowerCase().matches("(bank|account|credit|card|iban).*");
    }

    @Override
    public String mask(String value) {
        // Complex financial data masking logic
        return maskFinancialData(value);
    }

    @Override
    public int getPriority() {
        return 100; // High priority for financial data
    }
}
```

---

## Impact Analysis

| Approach    | Files Modified | Test Files   | Config Changes | Complexity | Risk     |
| ----------- | -------------- | ------------ | -------------- | ---------- | -------- |
| Config-only | 0              | 1 (optional) | 1              | Very Low   | Very Low |
| Enhanced    | 2              | 1            | 1              | Low        | Low      |
| Plugin      | 3+             | 2+           | 1              | Medium     | Medium   |

## Recommendations

### For Simple Cases (basic masking)

- **Use Approach 1**: Configuration-only extension
- **Time to implement**: 5 minutes
- **Suitable for**: Non-sensitive patterns, quick deployment

### For Production Use (sophisticated masking)

- **Use Approach 2**: Enhanced implementation
- **Time to implement**: 2-4 hours
- **Suitable for**: Financial data, regulatory compliance, production systems

### For Complex Scenarios (multiple providers)

- **Use Approach 3**: Plugin architecture
- **Time to implement**: 1-2 days
- **Suitable for**: Multiple masking providers, enterprise systems

## Testing Strategy

1. **Unit Tests**: Test each new masking method individually
2. **Integration Tests**: Test JSON processing with new fields
3. **Configuration Tests**: Verify field detection works correctly
4. **Edge Case Tests**: Handle null values, empty strings, malformed data

## Migration Path

1. Start with **Approach 1** for immediate needs
2. Upgrade to **Approach 2** when sophisticated masking is required
3. Move to **Approach 3** for enterprise-grade extensibility

The design's adherence to SOLID principles ensures that migration between approaches is seamless and non-breaking.
