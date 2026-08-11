package com.example.stockflow.product;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductValidationTests {

    private ValidatorFactory validatorFactory;
    private Validator validator;

    @BeforeEach
    void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterEach
    void tearDown() {
        validatorFactory.close();
    }

    @Test
    void validProductHasNoConstraintViolations() {
        Product product = new Product(
                "Mechanical Keyboard",
                "KEY-001",
                new BigDecimal("129.90"),
                10
        );

        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        assertTrue(violations.isEmpty());
        assertTrue(product.isActive());
    }

    @Test
    void invalidProductReportsExpectedFields() {
        Product product = new Product(" ", "", BigDecimal.ZERO, -1);

        Set<String> invalidFields = validator.validate(product).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertEquals(Set.of("name", "sku", "price", "stock"), invalidFields);
    }
}
