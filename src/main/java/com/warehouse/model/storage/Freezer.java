package com.warehouse.model.storage;

import com.warehouse.model.product.PerishableProduct;
import com.warehouse.model.product.Product;

/**
 * Freezer / Cold room - implements TemperatureControlled.
 * Demonstrates: Multiple interface implementation, ISP in action
 */
public class Freezer extends AbstractStorageUnit implements TemperatureControlled {

    private final double minTemperature;
    private final double maxTemperature;
    private double currentTemperature;
    private double targetTemperature;

    public Freezer(String id, String name, String location, int capacity,
                   double minTemperature, double maxTemperature, double initialTemperature) {
        super(id, name, location, capacity);
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.currentTemperature = initialTemperature;
        this.targetTemperature = initialTemperature;
    }

    /**
     * Only stores perishables that require refrigeration at matching temp requirements.
     */
    @Override
    public boolean canStore(Product product) {
        if (product instanceof PerishableProduct perishable) {
            return perishable.isRequiresRefrigeration()
                    && perishable.getRequiredTemperatureCelsius() >= minTemperature
                    && perishable.getRequiredTemperatureCelsius() <= maxTemperature;
        }
        return false;
    }

    @Override
    public String getStorageType() {
        return "FREEZER";
    }

    @Override
    public double getMinTemperatureCelsius() { return minTemperature; }

    @Override
    public double getMaxTemperatureCelsius() { return maxTemperature; }

    @Override
    public double getCurrentTemperatureCelsius() { return currentTemperature; }

    @Override
    public void setTargetTemperature(double celsius) {
        if (celsius < minTemperature || celsius > maxTemperature) {
            throw new IllegalArgumentException(
                    String.format("Target %.1f°C is outside this unit's range [%.1f°C - %.1f°C]",
                            celsius, minTemperature, maxTemperature));
        }
        this.targetTemperature = celsius;
        // Simulate gradual adjustment (in real system, this would be async)
        this.currentTemperature = celsius;
    }

    public double getTargetTemperature() { return targetTemperature; }

    // For testing/simulation
    public void simulateTemperatureDrift(double driftAmount) {
        this.currentTemperature += driftAmount;
    }

    @Override
    public String toString() {
        return super.toString() + " | Temp: " + getTemperatureStatus();
    }
}
