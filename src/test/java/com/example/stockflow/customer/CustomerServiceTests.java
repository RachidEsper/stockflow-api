package com.example.stockflow.customer;

import com.example.stockflow.customer.dto.CustomerRequest;
import com.example.stockflow.customer.dto.CustomerResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifica las reglas de negocio de {@link CustomerService} de forma aislada,
 * sustituyendo el repositorio real por un mock de Mockito.
 */
@ExtendWith(MockitoExtension.class)
class CustomerServiceTests {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void listsCustomersOrderedByLastNameAndFirstName() {
        Customer ada = persistedCustomer(1L, "Ada", "Lovelace", "ada@example.com");
        Customer alan = persistedCustomer(2L, "Alan", "Turing", "alan@example.com");
        when(customerRepository.findAllByOrderByLastNameAscFirstNameAsc())
                .thenReturn(List.of(ada, alan));

        List<CustomerResponse> result = customerService.findAll();

        assertEquals(List.of("Lovelace", "Turing"),
                result.stream().map(CustomerResponse::lastName).toList());
    }

    @Test
    void returnsCustomerById() {
        Customer customer = persistedCustomer(1L, "Ada", "Lovelace", "ada@example.com");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        CustomerResponse result = customerService.findById(1L);

        assertAll(
                () -> assertEquals(1L, result.id()),
                () -> assertEquals("Ada", result.firstName()),
                () -> assertEquals("Lovelace", result.lastName()),
                () -> assertEquals("ada@example.com", result.email()),
                () -> assertTrue(result.active())
        );
    }

    @Test
    void throwsWhenCustomerDoesNotExist() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        CustomerNotFoundException exception = assertThrows(
                CustomerNotFoundException.class,
                () -> customerService.findById(99L)
        );

        assertEquals("No se encontró el cliente con id 99", exception.getMessage());
    }

    @Test
    void createsCustomerWithNormalizedData() {
        CustomerRequest request = new CustomerRequest(
                "  Ada  ",
                "  Lovelace  ",
                "  ADA@Example.COM  "
        );
        when(customerRepository.existsByEmailIgnoreCase("ada@example.com")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 1L);
            return saved;
        });

        CustomerResponse result = customerService.create(request);

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerCaptor.capture());
        Customer saved = customerCaptor.getValue();
        assertAll(
                () -> assertEquals(1L, result.id()),
                () -> assertEquals("Ada", saved.getFirstName()),
                () -> assertEquals("Lovelace", saved.getLastName()),
                () -> assertEquals("ada@example.com", saved.getEmail()),
                () -> assertTrue(saved.isActive())
        );
    }

    @Test
    void rejectsDuplicateEmailWhenCreating() {
        CustomerRequest request = new CustomerRequest(
                "Ada",
                "Lovelace",
                "ADA@Example.COM"
        );
        when(customerRepository.existsByEmailIgnoreCase("ada@example.com")).thenReturn(true);

        DuplicateEmailException exception = assertThrows(
                DuplicateEmailException.class,
                () -> customerService.create(request)
        );

        assertEquals("Ya existe un cliente con el email ada@example.com", exception.getMessage());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void updatesExistingCustomer() {
        Customer customer = persistedCustomer(1L, "Augusta", "King", "ada@example.com");
        CustomerRequest request = new CustomerRequest(
                "  Ada  ",
                "  Lovelace  ",
                " ADA@EXAMPLE.COM "
        );
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.findByEmailIgnoreCase("ada@example.com"))
                .thenReturn(Optional.of(customer));
        when(customerRepository.save(customer)).thenReturn(customer);

        CustomerResponse result = customerService.update(1L, request);

        assertAll(
                () -> assertEquals("Ada", result.firstName()),
                () -> assertEquals("Lovelace", result.lastName()),
                () -> assertEquals("ada@example.com", result.email()),
                () -> assertTrue(result.active())
        );
    }

    @Test
    void rejectsAnotherCustomersEmailWhenUpdating() {
        Customer customer = persistedCustomer(1L, "Ada", "Lovelace", "ada@example.com");
        Customer conflictingCustomer = persistedCustomer(
                2L,
                "Alan",
                "Turing",
                "alan@example.com"
        );
        CustomerRequest request = new CustomerRequest(
                "Ada",
                "Lovelace",
                "ALAN@EXAMPLE.COM"
        );
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.findByEmailIgnoreCase("alan@example.com"))
                .thenReturn(Optional.of(conflictingCustomer));

        assertThrows(DuplicateEmailException.class,
                () -> customerService.update(1L, request));
        verify(customerRepository, never()).save(customer);
    }

    @Test
    void deactivatesCustomerWithoutDeletingIt() {
        Customer customer = persistedCustomer(1L, "Ada", "Lovelace", "ada@example.com");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(customer)).thenReturn(customer);

        customerService.deactivate(1L);

        assertFalse(customer.isActive());
        verify(customerRepository).save(customer);
        verify(customerRepository, never()).delete(any(Customer.class));
    }

    private Customer persistedCustomer(Long id, String firstName, String lastName, String email) {
        Customer customer = new Customer(firstName, lastName, email);
        ReflectionTestUtils.setField(customer, "id", id);
        return customer;
    }
}
