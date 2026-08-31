package com.example.stockflow.customer.dto;

/**
 * Representación pública de un cliente devuelta por la API.
 *
 * @param id identificador generado por PostgreSQL
 * @param firstName nombre del cliente
 * @param lastName apellido del cliente
 * @param email correo electrónico normalizado
 * @param active estado lógico del cliente
 */
public record CustomerResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        boolean active
) {
}
