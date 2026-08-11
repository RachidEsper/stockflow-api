package com.example.stockflow.product.dto;

import java.math.BigDecimal;

/**
 * Representación pública de un producto devuelta por la API.
 *
 * @param id identificador generado por PostgreSQL
 * @param name nombre visible
 * @param sku código interno normalizado
 * @param price precio unitario
 * @param stock cantidad disponible
 * @param active estado lógico del producto
 */
public record ProductResponse(
        Long id,
        String name,
        String sku,
        BigDecimal price,
        Integer stock,
        boolean active
) {
}
