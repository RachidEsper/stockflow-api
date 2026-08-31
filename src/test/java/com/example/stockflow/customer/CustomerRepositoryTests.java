package com.example.stockflow.customer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica las consultas personalizadas de {@link CustomerRepository} contra
 * PostgreSQL.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CustomerRepositoryTests {

    private final CustomerRepository customerRepository;

    CustomerRepositoryTests(@Autowired CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @BeforeEach
    void clearCustomers() {
        customerRepository.deleteAllInBatch();
    }

    @Test
    void findsCustomerByEmailIgnoringCase() {
        Customer customer = new Customer("Ada", "Lovelace", "ada@example.com");
        customerRepository.saveAndFlush(customer);

        Optional<Customer> result = customerRepository.findByEmailIgnoreCase("ADA@EXAMPLE.COM");

        assertTrue(result.isPresent());
        assertEquals("ada@example.com", result.orElseThrow().getEmail());
    }

    @Test
    void reportsExistingEmailIgnoringCase() {
        Customer customer = new Customer("Alan", "Turing", "alan@example.com");
        customerRepository.saveAndFlush(customer);

        boolean exists = customerRepository.existsByEmailIgnoreCase("ALAN@EXAMPLE.COM");

        assertTrue(exists);
    }

    @Test
    void returnsCustomersOrderedByLastNameAndFirstName() {
        Customer alan = new Customer("Alan", "Turing", "alan@example.com");
        Customer grace = new Customer("Grace", "Hopper", "grace@example.com");
        Customer ada = new Customer("Ada", "Lovelace", "ada@example.com");
        customerRepository.saveAllAndFlush(List.of(alan, grace, ada));

        List<String> names = customerRepository.findAllByOrderByLastNameAscFirstNameAsc().stream()
                .map(customer -> customer.getFirstName() + " " + customer.getLastName())
                .toList();

        assertEquals(List.of("Grace Hopper", "Ada Lovelace", "Alan Turing"), names);
    }
}
