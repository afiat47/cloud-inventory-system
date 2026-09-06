package com.example.inventory.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String product, int requested, int available) {
        super("Insufficient stock for %s: requested %d, available %d"
                .formatted(product, requested, available));
    }
}
