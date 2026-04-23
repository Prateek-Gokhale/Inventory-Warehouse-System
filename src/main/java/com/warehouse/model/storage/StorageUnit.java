package com.warehouse.model.storage;

import com.warehouse.model.product.Product;
import java.util.List;

/**
 * Core interface for all storage unit types.
 * Demonstrates: Interface Segregation (SOLID), Abstraction
 *
 * All storage units can store/retrieve products and report capacity,
 * but different units have different specialized capabilities.
 */
public interface StorageUnit {

    String getId();
    String getName();
    String getLocation();  // e.g., "Zone-A, Row-3, Bay-2"

    /**
     * Maximum number of product units this storage can hold.
     */
    int getCapacity();

    /**
     * Current number of product units stored.
     */
    int getCurrentLoad();

    default int getAvailableSpace() {
        return getCapacity() - getCurrentLoad();
    }

    default boolean isFull() {
        return getCurrentLoad() >= getCapacity();
    }

    default double getOccupancyPercent() {
        return (getCurrentLoad() * 100.0) / getCapacity();
    }

    /**
     * Check if this storage unit can hold a specific product type.
     */
    boolean canStore(Product product);

    /**
     * Add a product to this storage unit.
     * @throws IllegalStateException if unit is full or incompatible product
     */
    void addProduct(Product product, int quantity);

    /**
     * Remove a product from this storage unit.
     * @throws IllegalArgumentException if product not found or insufficient quantity
     */
    void removeProduct(String productId, int quantity);

    List<Product> getStoredProducts();

    String getStorageType();
}
