# Data Masking Extension Enhancement Plan

This document outlines potential enhancements for the `data-masking` extension based on a code review.

## 1. Configuration Refinement

**File**: `DataMaskingExtension.java`

**Observation**:
The `DataMaskingConfiguration` class is defined but not used within the `DataMaskingExtension`. Instead, configuration properties are injected directly into the extension using `@Setting`. This leads to redundant code and a slight deviation from the intended design of encapsulating configuration.

**Recommendation**:
Refactor the `DataMaskingExtension` to use the `DataMaskingConfiguration` object. This will involve:
1.  Removing the individual `@Setting` annotations for `maskingEnabled` and `fieldsToMask` from the extension.
2.  Injecting the `DataMaskingConfiguration` using `@Inject` or creating it within the `initialize` method.
3.  Accessing configuration properties through the `DataMaskingConfiguration` instance.

This will make the configuration handling cleaner and more maintainable.

## 2. Extensible Masking Strategy

**File**: `DataMaskingServiceImpl.java`

**Observation**:
The `maskValue` method in `DataMaskingServiceImpl` uses a hardcoded `if-else if` chain to select the masking logic based on field names. This approach is not easily extensible. Adding new masking rules requires modifying this core service class.

**Recommendation**:
Implement a Strategy Pattern for masking rules. This would involve:
1.  Defining a `MaskingStrategy` interface with a `mask(String value)` method.
2.  Creating concrete implementations of this interface for different masking types (e.g., `NameMaskingStrategy`, `EmailMaskingStrategy`).
3.  In `DataMaskingServiceImpl`, use a `Map<Predicate<String>, MaskingStrategy>` to register and select strategies. The predicate would check if a field name matches a certain criteria.

This will decouple the service from the masking logic, making it easy to add or modify masking rules without changing the service itself.

## 3. Input Type Flexibility

**File**: `DataMaskingTransformer.java`

**Observation**:
The `DataMaskingTransformer` is implemented as a `TypeTransformer<String, String>`. This restricts its use to data flows where the data is represented as a `String`. In many EDC scenarios, data is handled as an `InputStream` to support large files and streaming.

**Recommendation**:
Enhance the `DataMaskingTransformer` to support `InputStream`. This would involve:
1.  Changing the transformer to implement `TypeTransformer<InputStream, InputStream>`.
2.  In the `transform` method, read the content from the input `InputStream`, convert it to a `String`, apply the masking, and then write the masked string to a new `InputStream`.

This change would make the data masking extension more versatile and applicable to a wider range of data transfer scenarios within EDC.

## Step-by-Step Enhancement Plan

Here is a detailed plan to implement the proposed enhancements.

### Phase 1: Configuration Refinement

**Goal**: Refactor the extension to use a dedicated configuration object.

1.  **Modify `DataMaskingExtension.java`**:
    *   Remove the `@Setting` annotations and the corresponding `maskingEnabled` and `fieldsToMask` fields.
    *   Remove the `@Configuration` annotation from the `configuration` field. This field will be populated manually.
    *   In the `initialize` method, load the configuration using `context.getConfig().get("edc.data.masking", DataMaskingConfiguration.class)`.
    *   Update the logic to use the `configuration` object to access settings (e.g., `configuration.isMaskingEnabled()`).

2.  **Update Unit Tests**:
    *   Adjust the unit tests for `DataMaskingExtension` to mock the `ServiceExtensionContext` and `Config` objects to provide the `DataMaskingConfiguration`.

### Phase 2: Extensible Masking Strategy

**Goal**: Replace the `if-else` based masking logic with a more extensible strategy pattern.

1.  **Create `MaskingStrategy` SPI**:
    *   Create a new `spi` package: `org.eclipse.edc.connector.datamasking.spi`.
    *   Inside the `spi` package, define a `MaskingStrategy` interface with two methods: `canMask(String fieldName)` and `mask(String value)`.

2.  **Implement Concrete Strategies**:
    *   Create `NameMaskingStrategy`, `EmailMaskingStrategy`, and `PhoneNumberMaskingStrategy` classes that implement the `MaskingStrategy` interface.
    *   Move the corresponding masking logic from `DataMaskingServiceImpl` into these new classes.

3.  **Refactor `DataMaskingServiceImpl`**:
    *   Remove the private masking methods.
    *   Add a `List<MaskingStrategy>` field to hold the registered strategies.
    *   Update the constructor to accept this list of strategies.
    *   Modify the `maskValue` method to iterate through the strategies, use `canMask` to find the appropriate strategy, and then use it to `mask` the value.

4.  **Update `DataMaskingExtension`**:
    *   In the `initialize` method, create instances of the default strategies and pass them to the `DataMaskingServiceImpl` constructor.

5.  **Update Unit Tests**:
    *   Update the tests for `DataMaskingServiceImpl` to verify the new strategy-based logic.

### Phase 3: Input Type Flexibility

**Goal**: Enhance the transformer to support `InputStream` for broader compatibility.

1.  **Modify `DataMaskingTransformer.java`**:
    *   Change the class signature to implement `TypeTransformer<InputStream, InputStream>`.
    *   Update `getInputType()` and `getOutputType()` to return `InputStream.class`.
    *   In the `transform` method:
        *   Read the input `InputStream` into a `String`.
        *   Call the `dataMaskingService.mask()` method.
        *   Convert the resulting masked string back into an `InputStream`.
        *   Ensure proper handling of `IOException` and resource management (e.g., closing streams).

2.  **Update Unit Tests**:
    *   Modify the unit tests for `DataMaskingTransformer` to use `ByteArrayInputStream` as input and to read the output stream to assert the correctness of the transformation.
