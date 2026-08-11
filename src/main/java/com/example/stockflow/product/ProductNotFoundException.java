package com.example.stockflow.product;

/**
 * Indica que no existe un producto para el identificador solicitado.
 */
public class ProductNotFoundException extends RuntimeException {

    /**
     * Crea la excepción con un mensaje que identifica el producto ausente.
     *
     * @param id identificador que no pudo encontrarse
     */
    public ProductNotFoundException(Long id) {
        super("No se encontró el producto con id " + id);
    }
}
