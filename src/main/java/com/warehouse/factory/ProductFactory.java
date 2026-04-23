package com.warehouse.factory;

import com.warehouse.model.product.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * Factory for creating products from a generic config map.
 * Demonstrates: Factory Pattern, Open/Closed Principle
 *
 * New product types can be added by extending this factory,
 * without modifying existing creation logic.
 */
public class ProductFactory {

    public enum ProductType {
        PERISHABLE, ELECTRONIC, BULK
    }

    /**
     * Creates a product from a type enum and config map.
     * Real-world use: parsing JSON/CSV data from suppliers.
     */
    public static Product createProduct(ProductType type, Map<String, Object> config) {
        return switch (type) {
            case PERISHABLE -> createPerishable(config);
            case ELECTRONIC -> createElectronic(config);
            case BULK -> createBulk(config);
        };
    }

    private static PerishableProduct createPerishable(Map<String, Object> config) {
        return new PerishableProduct(
                (String) config.get("name"),
                (String) config.get("sku"),
                (double) config.get("price"),
                (int) config.get("quantity"),
                (String) config.get("supplierId"),
                (LocalDate) config.get("expiryDate"),
                (double) config.get("requiredTemperatureCelsius")
        );
    }

    private static ElectronicProduct createElectronic(Map<String, Object> config) {
        return new ElectronicProduct(
                (String) config.get("name"),
                (String) config.get("sku"),
                (double) config.get("price"),
                (int) config.get("quantity"),
                (String) config.get("supplierId"),
                (String) config.get("brand"),
                (String) config.get("modelNumber"),
                (int) config.get("warrantyMonths"),
                (boolean) config.get("isFragile"),
                (boolean) config.get("requiresAntiStaticStorage"),
                (double) config.get("weightKg")
        );
    }

    private static BulkProduct createBulk(Map<String, Object> config) {
        return new BulkProduct(
                (String) config.get("name"),
                (String) config.get("sku"),
                (double) config.get("price"),
                (int) config.get("quantity"),
                (String) config.get("supplierId"),
                (BulkProduct.Unit) config.get("unit"),
                (double) config.get("unitSize"),
                (boolean) config.get("isHazardous"),
                (boolean) config.get("isFlammable"),
                (double) config.get("maxStackHeightMeters")
        );
    }
}
