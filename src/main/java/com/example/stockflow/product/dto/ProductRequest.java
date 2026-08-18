package com.example.stockflow.product.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Datos aceptados por la API para crear o actualizar un producto.
 *
 * @param name nombre visible, obligatorio y de hasta 120 caracteres
 * @param description descripción opcional de hasta 500 caracteres
 * @param sku código interno único, obligatorio y de hasta 50 caracteres
 * @param price precio positivo con hasta dos decimales
 * @param stock cantidad disponible, igual o mayor que cero
 */
public record ProductRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 500) String description,
        @NotBlank @Size(max = 50) String sku,
        @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal price,
        @NotNull @PositiveOrZero Integer stock
) {
    public ProductRequest(String name, String sku, BigDecimal price, Integer stock) {
        this(name, null, sku, price, stock);
    }
}
