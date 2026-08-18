package com.example.stockflow.product.dto;

import java.math.BigDecimal;

/**
 * Representación pública de un producto devuelta por la API.
 *
 * @param id identificador generado por PostgreSQL
 * @param name nombre visible
 * @param description descripción opcional
 * @param sku código interno normalizado
 * @param price precio unitario
 * @param stock cantidad disponible
 * @param active estado lógico del producto
 */
public record ProductResponse(
        Long id,
        String name,
        String description,
        String sku,
        BigDecimal price,
        Integer stock,
        boolean active
) {
    public ProductResponse(Long id, String name, String sku, BigDecimal price, Integer stock, boolean active) {
        this(id, name, null, sku, price, stock, active);
    }
}
