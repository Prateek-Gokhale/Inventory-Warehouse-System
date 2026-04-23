package com.warehouse.model.storage;

import com.warehouse.model.product.Product;
import java.util.*;

/**
 * Abstract base class for all concrete storage units.
 * Handles common state and behavior shared across Shelf, Freezer, Vault.
 * Demonstrates: Abstraction, Template Method pattern idea
 */
public abstract class AbstractStorageUnit implements StorageUnit {

    private final String id;
    private final String name;
    private final String location;
    private final int capacity;

    // productId -> quantity stored in this unit
    protected final Map<String, Integer> storedQuantities = new LinkedHashMap<>();
    // productId -> Product reference
    protected final Map<String, Product> productRegistry = new LinkedHashMap<>();

    protected AbstractStorageUnit(String id, String name, String location, int capacity) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.capacity = capacity;
    }

    @Override
    public String getId() { return id; }

    @Override
    public String getName() { return name; }

    @Override
    public String getLocation() { return location; }

    @Override
    public int getCapacity() { return capacity; }

    @Override
    public int getCurrentLoad() {
        return storedQuantities.values().stream().mapToInt(Integer::intValue).sum();
    }

    @Override
    public void addProduct(Product product, int quantity) {
        if (!canStore(product)) {
            throw new IllegalStateException(
                    "Storage unit '" + name + "' cannot store product type: " + product.getProductType()
                    + ". Requirements: " + product.getStorageRequirements());
        }
        if (getCurrentLoad() + quantity > capacity) {
            throw new IllegalStateException(
                    "Insufficient space in '" + name + "'. Available: " + getAvailableSpace()
                    + ", Requested: " + quantity);
        }
        storedQuantities.merge(product.getId(), quantity, Integer::sum);
        productRegistry.putIfAbsent(product.getId(), product);
    }

    @Override
    public void removeProduct(String productId, int quantity) {
        Integer stored = storedQuantities.get(productId);
        if (stored == null || stored < quantity) {
            throw new IllegalArgumentException(
                    "Cannot remove " + quantity + " units of product " + productId
                    + ". Currently stored: " + (stored == null ? 0 : stored));
        }
        int remaining = stored - quantity;
        if (remaining == 0) {
            storedQuantities.remove(productId);
            productRegistry.remove(productId);
        } else {
            storedQuantities.put(productId, remaining);
        }
    }

    @Override
    public List<Product> getStoredProducts() {
        return new ArrayList<>(productRegistry.values());
    }

    public Map<String, Integer> getStoredQuantities() {
        return Collections.unmodifiableMap(storedQuantities);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s @ %s | %d/%d units (%.1f%% full)",
                getStorageType(), name, location, getCurrentLoad(), capacity, getOccupancyPercent());
    }
}
