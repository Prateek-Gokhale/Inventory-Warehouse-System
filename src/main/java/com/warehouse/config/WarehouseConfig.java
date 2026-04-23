package com.warehouse.config;

/**
 * Warehouse-wide configuration using Singleton pattern.
 * Demonstrates: Singleton Pattern — only one config instance per JVM.
 *
 * NOTE: This is a textbook Singleton for OOP demonstration.
 * In production Spring/Jakarta apps, you'd use dependency injection instead.
 */
public class WarehouseConfig {

    private static WarehouseConfig instance;

    private String warehouseName;
    private String location;
    private int defaultLowStockThreshold;
    private int nearExpiryDaysThreshold;
    private double maxStorageOccupancyPercent;

    // Private constructor - prevents direct instantiation
    private WarehouseConfig() {
        // Default configuration values
        this.warehouseName        = "Central Warehouse";
        this.location             = "Mumbai, India";
        this.defaultLowStockThreshold  = 10;
        this.nearExpiryDaysThreshold   = 30;
        this.maxStorageOccupancyPercent = 90.0;
    }

    /**
     * Returns the single instance, creating it if it doesn't exist.
     * Not thread-safe — use synchronized or enum-based Singleton in real code.
     */
    public static WarehouseConfig getInstance() {
        if (instance == null) {
            instance = new WarehouseConfig();
        }
        return instance;
    }

    public String getWarehouseName()              { return warehouseName; }
    public String getLocation()                   { return location; }
    public int getDefaultLowStockThreshold()      { return defaultLowStockThreshold; }
    public int getNearExpiryDaysThreshold()       { return nearExpiryDaysThreshold; }
    public double getMaxStorageOccupancyPercent() { return maxStorageOccupancyPercent; }

    public void setWarehouseName(String warehouseName)                     { this.warehouseName = warehouseName; }
    public void setLocation(String location)                               { this.location = location; }
    public void setDefaultLowStockThreshold(int threshold)                 { this.defaultLowStockThreshold = threshold; }
    public void setNearExpiryDaysThreshold(int days)                       { this.nearExpiryDaysThreshold = days; }
    public void setMaxStorageOccupancyPercent(double maxOccupancyPercent)  { this.maxStorageOccupancyPercent = maxOccupancyPercent; }

    @Override
    public String toString() {
        return String.format("WarehouseConfig { name='%s', location='%s', lowStockThreshold=%d, nearExpiryDays=%d }",
                warehouseName, location, defaultLowStockThreshold, nearExpiryDaysThreshold);
    }
}
