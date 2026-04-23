package com.warehouse.model.product;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Perishable product - food, medicine, etc.
 * Demonstrates: Inheritance, method overriding (Polymorphism)
 */
public class PerishableProduct extends Product {

    private LocalDate expiryDate;
    private double requiredTemperatureCelsius;
    private boolean requiresRefrigeration;

    public PerishableProduct(String name, String sku, double price, int quantity,
                             String supplierId, LocalDate expiryDate,
                             double requiredTemperatureCelsius) {
        super(name, sku, price, quantity, supplierId);
        this.expiryDate = expiryDate;
        this.requiredTemperatureCelsius = requiredTemperatureCelsius;
        this.requiresRefrigeration = requiredTemperatureCelsius < 15.0;
    }

    @Override
    public String getProductType() {
        return "PERISHABLE";
    }

    @Override
    public boolean requiresSpecialStorage() {
        return requiresRefrigeration;
    }

    @Override
    public String getStorageRequirements() {
        if (requiresRefrigeration) {
            return String.format("Refrigerated storage at %.1f°C or below", requiredTemperatureCelsius);
        }
        return String.format("Cool dry storage below %.1f°C", requiredTemperatureCelsius);
    }

    /**
     * Storage cost increases as product nears expiry.
     * Demonstrates: Business logic in overridden method
     */
    @Override
    public double calculateStorageCost() {
        long daysUntilExpiry = ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
        double baseCost = requiresRefrigeration ? 5.0 : 2.0;

        // Urgent handling surcharge for near-expiry items
        if (daysUntilExpiry <= 7) {
            return baseCost * 1.5;
        } else if (daysUntilExpiry <= 30) {
            return baseCost * 1.2;
        }
        return baseCost;
    }

    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }

    public long getDaysUntilExpiry() {
        return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }

    public boolean isNearExpiry(int daysThreshold) {
        return getDaysUntilExpiry() <= daysThreshold && !isExpired();
    }

    public LocalDate getExpiryDate() { return expiryDate; }
    public double getRequiredTemperatureCelsius() { return requiredTemperatureCelsius; }
    public boolean isRequiresRefrigeration() { return requiresRefrigeration; }

    @Override
    public String toString() {
        return super.toString() + String.format(" | Expires: %s | Temp: %.1f°C",
                expiryDate, requiredTemperatureCelsius);
    }
}
