package com.example.stockflow.product;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(Long productId, int available, int requested) {
        super("Stock insuficiente para el producto con id " + productId
                + ": disponible " + available + ", solicitado " + requested);
    }
}
