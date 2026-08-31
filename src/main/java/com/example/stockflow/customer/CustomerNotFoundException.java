package com.example.stockflow.customer;

/**
 * Indica que no existe un cliente para el identificador solicitado.
 */
public class CustomerNotFoundException extends RuntimeException {

    /**
     * Crea la excepción con un mensaje que identifica el cliente ausente.
     *
     * @param id identificador que no pudo encontrarse
     */
    public CustomerNotFoundException(Long id) {
        super("No se encontró el cliente con id " + id);
    }
}
