package com.warehouse.model.product;

/**
 * Electronic product - devices, components, etc.
 * Demonstrates: Inheritance, Polymorphism with different storage logic
 */
public class ElectronicProduct extends Product {

    private String brand;
    private String modelNumber;
    private int warrantyMonths;
    private boolean isFragile;
    private boolean requiresAntiStaticStorage;
    private double weightKg;

    public ElectronicProduct(String name, String sku, double price, int quantity,
                              String supplierId, String brand, String modelNumber,
                              int warrantyMonths, boolean isFragile,
                              boolean requiresAntiStaticStorage, double weightKg) {
        super(name, sku, price, quantity, supplierId);
        this.brand = brand;
        this.modelNumber = modelNumber;
        this.warrantyMonths = warrantyMonths;
        this.isFragile = isFragile;
        this.requiresAntiStaticStorage = requiresAntiStaticStorage;
        this.weightKg = weightKg;
    }

    @Override
    public String getProductType() {
        return "ELECTRONIC";
    }

    @Override
    public boolean requiresSpecialStorage() {
        return isFragile || requiresAntiStaticStorage;
    }

    @Override
    public String getStorageRequirements() {
        if (requiresAntiStaticStorage && isFragile) {
            return "Anti-static padded shelving, handle with care";
        } else if (requiresAntiStaticStorage) {
            return "Anti-static storage required";
        } else if (isFragile) {
            return "Padded storage, fragile handling";
        }
        return "Standard shelving";
    }

    /**
     * Cost based on fragility and weight.
     * Demonstrates: Different business logic per product type (Polymorphism)
     */
    @Override
    public double calculateStorageCost() {
        double baseCost = 1.0 + (weightKg * 0.1);
        if (requiresAntiStaticStorage) baseCost += 3.0;
        if (isFragile) baseCost += 2.0;
        return baseCost;
    }

    public String getBrand() { return brand; }
    public String getModelNumber() { return modelNumber; }
    public int getWarrantyMonths() { return warrantyMonths; }
    public boolean isFragile() { return isFragile; }
    public boolean isRequiresAntiStaticStorage() { return requiresAntiStaticStorage; }
    public double getWeightKg() { return weightKg; }

    @Override
    public String toString() {
        return super.toString() + String.format(" | Brand: %s | Model: %s | Warranty: %d mo",
                brand, modelNumber, warrantyMonths);
    }
}
