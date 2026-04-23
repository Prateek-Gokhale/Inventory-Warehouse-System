package com.warehouse.service.impl;

import com.warehouse.exception.ProductNotFoundException;
import com.warehouse.exception.StorageException;
import com.warehouse.model.product.Product;
import com.warehouse.model.storage.StorageUnit;
import com.warehouse.model.storage.TemperatureControlled;
import com.warehouse.observer.StockEvent;
import com.warehouse.observer.StockObserver;
import com.warehouse.repository.ProductRepository;
import com.warehouse.repository.StorageRepository;
import com.warehouse.service.StorageService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Storage management service implementation.
 * Demonstrates:
 * - Dependency Inversion: depends on interfaces not concretions
 * - Single Responsibility: handles physical placement, not inventory counts
 * - Open/Closed: new storage types work without changing this class
 */
public class StorageServiceImpl implements StorageService {

    private final StorageRepository storageRepository;
    private final ProductRepository productRepository;
    private final List<StockObserver> observers = new ArrayList<>();

    public StorageServiceImpl(StorageRepository storageRepository,
                               ProductRepository productRepository) {
        this.storageRepository = storageRepository;
        this.productRepository = productRepository;
    }

    @Override
    public StorageUnit addStorageUnit(StorageUnit unit) {
        return storageRepository.save(unit);
    }

    @Override
    public StorageUnit getStorageUnit(String unitId) {
        return storageRepository.findById(unitId)
                .orElseThrow(() -> new StorageException("Storage unit not found: " + unitId));
    }

    @Override
    public List<StorageUnit> getAllStorageUnits() {
        return storageRepository.findAll();
    }

    @Override
    public void storeProduct(String productId, String storageUnitId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        StorageUnit unit = getStorageUnit(storageUnitId);

        if (!unit.canStore(product)) {
            throw new StorageException(
                    "Storage unit '" + unit.getName() + "' is incompatible with product '"
                    + product.getName() + "'. Requirements: " + product.getStorageRequirements());
        }

        unit.addProduct(product, quantity);
        storageRepository.save(unit);

        // Alert if storage is now full
        if (unit.isFull()) {
            notifyObservers(new StockEvent(
                    StockEvent.EventType.STORAGE_FULL,
                    productId, product.getName(), product.getQuantity(),
                    "Storage unit '" + unit.getName() + "' is now full"
            ));
        }
    }

    @Override
    public void retrieveProduct(String productId, String storageUnitId, int quantity) {
        StorageUnit unit = getStorageUnit(storageUnitId);
        unit.removeProduct(productId, quantity);
        storageRepository.save(unit);
    }

    /**
     * Finds the best available unit for a product.
     * Strategy: compatible unit with most free space.
     * Demonstrates: Polymorphism — canStore() works correctly across all unit types
     */
    @Override
    public Optional<StorageUnit> findBestStorageFor(Product product) {
        return storageRepository.findAvailableUnits().stream()
                .filter(unit -> unit.canStore(product))
                .max(Comparator.comparingInt(StorageUnit::getAvailableSpace));
    }

    @Override
    public List<StorageUnit> findCompatibleStorage(Product product) {
        return storageRepository.findAll().stream()
                .filter(unit -> unit.canStore(product))
                .collect(Collectors.toList());
    }

    @Override
    public List<StorageUnit> getAvailableUnits() {
        return storageRepository.findAvailableUnits();
    }

    @Override
    public void generateStorageReport() {
        List<StorageUnit> units = storageRepository.findAll();

        System.out.println("\n" + "=".repeat(70));
        System.out.println("                    WAREHOUSE STORAGE REPORT");
        System.out.println("=".repeat(70));

        if (units.isEmpty()) {
            System.out.println("  No storage units registered.");
        }

        for (StorageUnit unit : units) {
            System.out.println("\n  " + unit);

            // Show temperature if applicable
            if (unit instanceof TemperatureControlled tc) {
                System.out.println("    Temperature: " + tc.getTemperatureStatus());
            }

            // Show stored products
            List<Product> products = unit.getStoredProducts();
            if (products.isEmpty()) {
                System.out.println("    [Empty]");
            } else {
                for (Product p : products) {
                    System.out.printf("    - %s%n", p.getName());
                }
            }
        }

        System.out.println("\n" + "=".repeat(70));
        long fullUnits = units.stream().filter(StorageUnit::isFull).count();
        System.out.printf("  Total units: %d | Full: %d | Available: %d%n",
                units.size(), fullUnits, units.size() - fullUnits);
        System.out.println("=".repeat(70) + "\n");
    }

    public void registerObserver(StockObserver observer) {
        observers.add(observer);
    }

    private void notifyObservers(StockEvent event) {
        for (StockObserver observer : observers) {
            observer.onStockEvent(event);
        }
    }
}
