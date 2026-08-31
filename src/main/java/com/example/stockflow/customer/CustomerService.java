package com.example.stockflow.customer;

import com.example.stockflow.customer.dto.CustomerRequest;
import com.example.stockflow.customer.dto.CustomerResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Implementa los casos de uso y reglas de negocio de los clientes.
 */
@Service
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;

    /**
     * Crea el servicio con el repositorio que administra la persistencia.
     *
     * @param customerRepository repositorio de clientes
     */
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * Lista todos los clientes, incluidos los inactivos, ordenados por apellido
     * y nombre.
     *
     * @return representaciones públicas de los clientes
     */
    public List<CustomerResponse> findAll() {
        return customerRepository.findAllByOrderByLastNameAscFirstNameAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Busca un cliente por su identificador.
     *
     * @param id identificador persistente
     * @return cliente encontrado
     * @throws CustomerNotFoundException cuando el identificador no existe
     */
    public CustomerResponse findById(Long id) {
        return toResponse(requireCustomer(id));
    }

    /**
     * Crea un cliente activo después de normalizar y comprobar su email.
     *
     * @param request datos validados del cliente
     * @return cliente persistido
     * @throws DuplicateEmailException cuando el email ya está registrado
     */
    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        ensureEmailIsAvailableForCreate(normalizedEmail);

        Customer customer = new Customer(
                normalizeName(request.firstName()),
                normalizeName(request.lastName()),
                normalizedEmail
        );

        return toResponse(customerRepository.save(customer));
    }

    /**
     * Reemplaza los datos editables de un cliente existente, manteniendo su
     * identificador y estado actuales.
     *
     * @param id identificador del cliente que se actualizará
     * @param request nuevos datos validados
     * @return cliente actualizado
     * @throws CustomerNotFoundException cuando el cliente no existe
     * @throws DuplicateEmailException cuando el nuevo email pertenece a otro cliente
     */
    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = requireCustomer(id);
        String normalizedEmail = normalizeEmail(request.email());
        ensureEmailIsAvailableForUpdate(normalizedEmail, id);

        customer.setFirstName(normalizeName(request.firstName()));
        customer.setLastName(normalizeName(request.lastName()));
        customer.setEmail(normalizedEmail);

        return toResponse(customerRepository.save(customer));
    }

    /**
     * Desactiva lógicamente un cliente y conserva su fila.
     *
     * @param id identificador del cliente que se desactivará
     * @throws CustomerNotFoundException cuando el cliente no existe
     */
    @Transactional
    public void deactivate(Long id) {
        Customer customer = requireCustomer(id);
        customer.deactivate();
        customerRepository.save(customer);
    }

    private Customer requireCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    private void ensureEmailIsAvailableForCreate(String email) {
        if (customerRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateEmailException(email);
        }
    }

    private void ensureEmailIsAvailableForUpdate(String email, Long currentCustomerId) {
        customerRepository.findByEmailIgnoreCase(email)
                .filter(existing -> !Objects.equals(existing.getId(), currentCustomerId))
                .ifPresent(existing -> {
                    throw new DuplicateEmailException(email);
                });
    }

    private String normalizeName(String name) {
        return name.trim();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.isActive()
        );
    }
}
