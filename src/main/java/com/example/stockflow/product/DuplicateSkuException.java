package com.example.stockflow.product;

/**
 * Indica que un SKU ya pertenece a otro producto.
 */
public class DuplicateSkuException extends RuntimeException {

    /**
     * Crea la excepción con el SKU que produjo el conflicto.
     *
     * @param sku código interno duplicado
     */
    public DuplicateSkuException(String sku) {
        super("Ya existe un producto con el SKU " + sku);
    }
}
