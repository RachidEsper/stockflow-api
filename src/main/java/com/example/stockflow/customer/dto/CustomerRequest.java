package com.example.stockflow.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos aceptados por la API para crear o actualizar un cliente.
 *
 * @param firstName nombre obligatorio de hasta 120 caracteres
 * @param lastName apellido obligatorio de hasta 120 caracteres
 * @param email correo electrónico válido, único y de hasta 254 caracteres
 */
public record CustomerRequest(
        @NotBlank @Size(max = 120) String firstName,
        @NotBlank @Size(max = 120) String lastName,
        @NotBlank @Email @Size(max = 254) String email
) {
}
