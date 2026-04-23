package com.warehouse.service;

import com.warehouse.model.product.Product;
import com.warehouse.observer.StockObserver;

import java.util.List;

/**
 * Inventory management service interface.
 * Demonstrates: Single Responsibility (inventory operations only),
 * Dependency Inversion (callers depend on this interface)
 */
public interface InventoryService {

    // Product CRUD
    Product addProduct(Product product);
    Product getProduct(String productId);
    List<Product> getAllProducts();
    void removeProduct(String productId);

    // Stock management
    void receiveStock(String productId, int quantity);
    void dispatchStock(String productId, int quantity);
    void transferStock(String fromProductId, String toProductId, int quantity);

    // Queries
    List<Product> getLowStockProducts(int threshold);
    List<Product> getOutOfStockProducts();
    List<Product> getProductsByType(String productType);
    List<Product> getProductsBySupplier(String supplierId);

    // Observer registration
    void registerObserver(StockObserver observer);
    void removeObserver(StockObserver observer);
}
