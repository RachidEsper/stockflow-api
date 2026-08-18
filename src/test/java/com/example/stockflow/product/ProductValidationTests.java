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

/**
 * Comprueba las restricciones de Bean Validation declaradas en {@link Product}
 * sin iniciar Spring ni conectarse a la base de datos.
 */
class ProductValidationTests {

    private ValidatorFactory validatorFactory;
    private Validator validator;

    /**
     * Crea un validador nuevo antes de cada escenario.
     */
    @BeforeEach
    void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    /**
     * Libera los recursos del proveedor de validación después de cada prueba.
     */
    @AfterEach
    void tearDown() {
        validatorFactory.close();
    }

    /**
     * Comprueba que un producto completo y coherente no genere infracciones y
     * se cree activo de forma predeterminada.
     */
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

    /**
     * Comprueba que nombre, SKU, precio y stock informen errores cuando reciben
     * valores inválidos.
     */
    @Test
    void invalidProductReportsExpectedFields() {
        Product product = new Product(" ", "", BigDecimal.ZERO, -1);

        Set<String> invalidFields = validator.validate(product).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertEquals(Set.of("name", "sku", "price", "stock"), invalidFields);
    }
}
