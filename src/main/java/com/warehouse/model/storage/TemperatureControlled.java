package com.warehouse.model.storage;

/**
 * Separate interface for temperature-controlled storage.
 * Demonstrates: Interface Segregation Principle (SOLID)
 *
 * We do NOT force Shelf to implement this — only Freezer and ColdRoom will.
 * This is the key ISP example: clients should not depend on methods they don't use.
 */
public interface TemperatureControlled {

    double getMinTemperatureCelsius();
    double getMaxTemperatureCelsius();
    double getCurrentTemperatureCelsius();

    void setTargetTemperature(double celsius);

    default boolean isTemperatureInRange() {
        double current = getCurrentTemperatureCelsius();
        return current >= getMinTemperatureCelsius() && current <= getMaxTemperatureCelsius();
    }

    default String getTemperatureStatus() {
        if (isTemperatureInRange()) {
            return String.format("OK (%.1f°C)", getCurrentTemperatureCelsius());
        }
        return String.format("ALERT! %.1f°C out of range [%.1f°C - %.1f°C]",
                getCurrentTemperatureCelsius(), getMinTemperatureCelsius(), getMaxTemperatureCelsius());
    }
}
