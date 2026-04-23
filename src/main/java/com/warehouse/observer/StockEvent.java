package com.warehouse.observer;

import java.time.LocalDateTime;

/**
 * Immutable event object passed to observers.
 * Demonstrates: Value Object, immutability
 */
public class StockEvent {

    public enum EventType {
        LOW_STOCK,
        OUT_OF_STOCK,
        STOCK_ADDED,
        STOCK_REMOVED,
        PRODUCT_EXPIRED,
        NEAR_EXPIRY,
        STORAGE_FULL,
        TEMPERATURE_ALERT
    }

    private final EventType type;
    private final String productId;
    private final String productName;
    private final int currentQuantity;
    private final String message;
    private final LocalDateTime occurredAt;

    public StockEvent(EventType type, String productId, String productName,
                      int currentQuantity, String message) {
        this.type = type;
        this.productId = productId;
        this.productName = productName;
        this.currentQuantity = currentQuantity;
        this.message = message;
        this.occurredAt = LocalDateTime.now();
    }

    public EventType getType() { return type; }
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getCurrentQuantity() { return currentQuantity; }
    public String getMessage() { return message; }
    public LocalDateTime getOccurredAt() { return occurredAt; }

    @Override
    public String toString() {
        return String.format("[%s] %s | Product: %s | Qty: %d | %s",
                occurredAt, type, productName, currentQuantity, message);
    }
}
