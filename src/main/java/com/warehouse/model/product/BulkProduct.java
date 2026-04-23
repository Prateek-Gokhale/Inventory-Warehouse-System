package com.warehouse.model.product;

/**
 * Bulk commodity product - raw materials, grains, liquids etc.
 * Demonstrates: Third concrete subclass showing full Polymorphism
 */
public class BulkProduct extends Product {

    public enum Unit {
        KG, LITER, CUBIC_METER, TON
    }

    private Unit unit;
    private double unitSize;          // e.g., 25 (kg per bag)
    private boolean isHazardous;
    private boolean isFlammable;
    private double maxStackHeightMeters;

    public BulkProduct(String name, String sku, double price, int quantity,
                       String supplierId, Unit unit, double unitSize,
                       boolean isHazardous, boolean isFlammable,
                       double maxStackHeightMeters) {
        super(name, sku, price, quantity, supplierId);
        this.unit = unit;
        this.unitSize = unitSize;
        this.isHazardous = isHazardous;
        this.isFlammable = isFlammable;
        this.maxStackHeightMeters = maxStackHeightMeters;
    }

    @Override
    public String getProductType() {
        return "BULK";
    }

    @Override
    public boolean requiresSpecialStorage() {
        return isHazardous || isFlammable;
    }

    @Override
    public String getStorageRequirements() {
        if (isFlammable && isHazardous) {
            return "Isolated hazmat zone, fire suppression required, no ignition sources";
        } else if (isFlammable) {
            return "Flammable storage zone, no ignition sources, ventilated";
        } else if (isHazardous) {
            return "Hazmat zone, sealed containers, ventilated";
        }
        return String.format("Standard bulk storage, max stack %.1fm", maxStackHeightMeters);
    }

    @Override
    public double calculateStorageCost() {
        double baseCost = unitSize * 0.05;
        if (isHazardous) baseCost *= 3.0;
        else if (isFlammable) baseCost *= 2.5;
        return baseCost;
    }

    public double getTotalWeight() {
        return getQuantity() * unitSize;
    }

    public Unit getUnit() { return unit; }
    public double getUnitSize() { return unitSize; }
    public boolean isHazardous() { return isHazardous; }
    public boolean isFlammable() { return isFlammable; }
    public double getMaxStackHeightMeters() { return maxStackHeightMeters; }

    @Override
    public String toString() {
        return super.toString() + String.format(" | Unit: %.1f %s | Hazardous: %s | Flammable: %s",
                unitSize, unit, isHazardous, isFlammable);
    }
}
