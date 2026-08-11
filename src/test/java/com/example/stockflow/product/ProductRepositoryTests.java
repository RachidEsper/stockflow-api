package com.example.stockflow.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica las consultas personalizadas de {@link ProductRepository} contra
 * PostgreSQL. Cada prueba se ejecuta dentro de una transacción que se revierte
 * automáticamente al finalizar.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryTests {

    private final ProductRepository productRepository;

    /**
     * Crea la prueba con el repositorio configurado por Spring Data JPA.
     *
     * @param productRepository repositorio real conectado a la base de pruebas
     */
    ProductRepositoryTests(@Autowired ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Limpia temporalmente la tabla antes de cada escenario. La transacción de
     * prueba restaura cualquier dato previo cuando finaliza el escenario.
     */
    @BeforeEach
    void clearProducts() {
        productRepository.deleteAllInBatch();
    }

    /**
     * Comprueba que una búsqueda encuentre el SKU aunque el texto consultado
     * utilice una combinación diferente de mayúsculas y minúsculas.
     */
    @Test
    void findsProductBySkuIgnoringCase() {
        Product product = new Product(
                "Mechanical Keyboard",
                "KEY-001",
                new BigDecimal("129.90"),
                10
        );
        productRepository.saveAndFlush(product);

        Optional<Product> result = productRepository.findBySkuIgnoreCase("key-001");

        assertTrue(result.isPresent());
        assertEquals("KEY-001", result.orElseThrow().getSku());
    }

    /**
     * Comprueba que la consulta de existencia detecte un SKU registrado sin
     * depender de cómo se escriban sus mayúsculas.
     */
    @Test
    void reportsExistingSkuIgnoringCase() {
        Product product = new Product(
                "Wireless Mouse",
                "MOU-001",
                new BigDecimal("59.90"),
                5
        );
        productRepository.saveAndFlush(product);

        boolean exists = productRepository.existsBySkuIgnoreCase("mou-001");

        assertTrue(exists);
    }

    /**
     * Comprueba que el listado devuelva los productos ordenados por nombre de
     * forma ascendente.
     */
    @Test
    void returnsProductsOrderedByName() {
        Product mouse = new Product(
                "Wireless Mouse",
                "MOU-001",
                new BigDecimal("59.90"),
                5
        );
        Product keyboard = new Product(
                "Mechanical Keyboard",
                "KEY-001",
                new BigDecimal("129.90"),
                10
        );
        productRepository.saveAllAndFlush(List.of(mouse, keyboard));

        List<String> names = productRepository.findAllByOrderByNameAsc().stream()
                .map(Product::getName)
                .toList();

        assertEquals(List.of("Mechanical Keyboard", "Wireless Mouse"), names);
    }
}
