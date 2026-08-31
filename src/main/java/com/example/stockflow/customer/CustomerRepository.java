package com.example.stockflow.customer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Proporciona acceso persistente a los clientes mediante Spring Data JPA.
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Busca un cliente por email sin distinguir mayúsculas y minúsculas.
     *
     * @param email correo electrónico del cliente
     * @return el cliente encontrado o un valor vacío si no existe
     */
    Optional<Customer> findByEmailIgnoreCase(String email);

    /**
     * Comprueba si un email ya está registrado sin distinguir mayúsculas y
     * minúsculas.
     *
     * @param email correo electrónico que se desea comprobar
     * @return {@code true} cuando el email ya pertenece a un cliente
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Recupera todos los clientes ordenados por apellido y nombre.
     *
     * @return clientes activos e inactivos ordenados alfabéticamente
     */
    List<Customer> findAllByOrderByLastNameAscFirstNameAsc();
}
