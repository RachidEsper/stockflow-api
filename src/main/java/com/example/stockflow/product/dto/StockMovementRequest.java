package com.example.stockflow.product.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StockMovementRequest(
        @NotNull @Positive Integer quantity
) {
}
