package com.warehouse.service.impl;

import com.warehouse.exception.InsufficientStockException;
import com.warehouse.exception.ProductNotFoundException;
import com.warehouse.model.product.Product;
import com.warehouse.observer.StockEvent;
import com.warehouse.observer.StockObserver;
import com.warehouse.repository.ProductRepository;
import com.warehouse.service.InventoryService;

import java.util.ArrayList;
import java.util.List;

/**
 * Core inventory service implementation.
 * Demonstrates:
 * - Dependency Inversion: depends on ProductRepository interface, not InMemoryProductRepository
 * - Observer Pattern: notifies registered observers on stock changes
 * - Single Responsibility: only manages inventory, not storage placement
 */
public class InventoryServiceImpl implements InventoryService {

    private final ProductRepository productRepository;
    private final List<StockObserver> observers = new ArrayList<>();
    private final int lowStockThreshold;

    public InventoryServiceImpl(ProductRepository productRepository, int lowStockThreshold) {
        this.productRepository = productRepository;
        this.lowStockThreshold = lowStockThreshold;
    }

    @Override
    public Product addProduct(Product product) {
        Product saved = productRepository.save(product);
        notifyObservers(new StockEvent(
                StockEvent.EventType.STOCK_ADDED,
                product.getId(), product.getName(),
                product.getQuantity(),
                "New product registered with qty: " + product.getQuantity()
        ));
        return saved;
    }

    @Override
    public Product getProduct(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public void removeProduct(String productId) {
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }
        productRepository.deleteById(productId);
    }

    @Override
    public void receiveStock(String productId, int quantity) {
        Product product = getProduct(productId);
        product.updateQuantity(quantity);
        productRepository.save(product);

        notifyObservers(new StockEvent(
                StockEvent.EventType.STOCK_ADDED,
                productId, product.getName(),
                product.getQuantity(),
                "Stock received: +" + quantity
        ));
    }

    @Override
    public void dispatchStock(String productId, int quantity) {
        Product product = getProduct(productId);

        if (product.getQuantity() < quantity) {
            throw new InsufficientStockException(product.getName(), quantity, product.getQuantity());
        }

        product.updateQuantity(-quantity);
        productRepository.save(product);

        notifyObservers(new StockEvent(
                StockEvent.EventType.STOCK_REMOVED,
                productId, product.getName(),
                product.getQuantity(),
                "Stock dispatched: -" + quantity
        ));

        // Check and trigger low stock / out of stock alerts
        checkStockAlerts(product);
    }

    @Override
    public void transferStock(String fromProductId, String toProductId, int quantity) {
        dispatchStock(fromProductId, quantity);
        receiveStock(toProductId, quantity);
    }

    @Override
    public List<Product> getLowStockProducts(int threshold) {
        return productRepository.findLowStock(threshold);
    }

    @Override
    public List<Product> getOutOfStockProducts() {
        return productRepository.findOutOfStock();
    }

    @Override
    public List<Product> getProductsByType(String productType) {
        return productRepository.findByProductType(productType);
    }

    @Override
    public List<Product> getProductsBySupplier(String supplierId) {
        return productRepository.findBySupplierId(supplierId);
    }

    @Override
    public void registerObserver(StockObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(StockObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers(StockEvent event) {
        for (StockObserver observer : observers) {
            observer.onStockEvent(event);
        }
    }

    private void checkStockAlerts(Product product) {
        if (product.isOutOfStock()) {
            notifyObservers(new StockEvent(
                    StockEvent.EventType.OUT_OF_STOCK,
                    product.getId(), product.getName(), 0,
                    "CRITICAL: Product is out of stock!"
            ));
        } else if (product.isLowStock(lowStockThreshold)) {
            notifyObservers(new StockEvent(
                    StockEvent.EventType.LOW_STOCK,
                    product.getId(), product.getName(), product.getQuantity(),
                    "WARNING: Stock below threshold of " + lowStockThreshold
            ));
        }
    }
}
