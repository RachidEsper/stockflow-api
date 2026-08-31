package com.example.stockflow.customer;

/**
 * Indica que un email ya pertenece a otro cliente.
 */
public class DuplicateEmailException extends RuntimeException {

    /**
     * Crea la excepción con el email que produjo el conflicto.
     *
     * @param email correo electrónico duplicado
     */
    public DuplicateEmailException(String email) {
        super("Ya existe un cliente con el email " + email);
    }
}
