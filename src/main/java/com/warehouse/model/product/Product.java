package com.warehouse.model.product;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Abstract base class for all product types.
 * Demonstrates: Abstraction, Inheritance base
 */
public abstract class Product {

    private final String id;
    private String name;
    private String sku;
    private double price;
    private int quantity;
    private String supplierId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected Product(String name, String sku, double price, int quantity, String supplierId) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.sku = sku;
        this.price = price;
        this.quantity = quantity;
        this.supplierId = supplierId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Abstract methods - each product type must implement these
    public abstract String getProductType();
    public abstract boolean requiresSpecialStorage();
    public abstract String getStorageRequirements();
    public abstract double calculateStorageCost();

    // Common behavior
    public void updateQuantity(int delta) {
        if (this.quantity + delta < 0) {
            throw new IllegalArgumentException("Quantity cannot go negative. Current: " + this.quantity + ", Delta: " + delta);
        }
        this.quantity += delta;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isLowStock(int threshold) {
        return this.quantity <= threshold;
    }

    public boolean isOutOfStock() {
        return this.quantity == 0;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getSku() { return sku; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getSupplierId() { return supplierId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setName(String name) { this.name = name; this.updatedAt = LocalDateTime.now(); }
    public void setPrice(double price) { this.price = price; this.updatedAt = LocalDateTime.now(); }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }

    @Override
    public String toString() {
        return String.format("[%s] %s | SKU: %s | Qty: %d | Price: $%.2f",
                getProductType(), name, sku, quantity, price);
    }
}
