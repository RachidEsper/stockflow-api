package com.example.stockflow.customer;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprueba las restricciones de Bean Validation declaradas en
 * {@link Customer} sin iniciar Spring ni conectarse a la base de datos.
 */
class CustomerValidationTests {

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
    void validCustomerHasNoConstraintViolations() {
        Customer customer = new Customer("Ada", "Lovelace", "ada@example.com");

        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);

        assertTrue(violations.isEmpty());
        assertTrue(customer.isActive());
    }

    @Test
    void invalidCustomerReportsExpectedFields() {
        Customer customer = new Customer(" ", "", "invalid-email");

        Set<String> invalidFields = validator.validate(customer).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertEquals(Set.of("firstName", "lastName", "email"), invalidFields);
    }
}
