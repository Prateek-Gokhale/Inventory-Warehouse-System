package com.warehouse.service;

import com.warehouse.model.product.Product;
import com.warehouse.model.storage.StorageUnit;

import java.util.List;
import java.util.Optional;

/**
 * Storage management service interface.
 * Demonstrates: SRP - this only deals with physical storage, not inventory counts
 */
public interface StorageService {

    StorageUnit addStorageUnit(StorageUnit unit);
    StorageUnit getStorageUnit(String unitId);
    List<StorageUnit> getAllStorageUnits();

    void storeProduct(String productId, String storageUnitId, int quantity);
    void retrieveProduct(String productId, String storageUnitId, int quantity);

    Optional<StorageUnit> findBestStorageFor(Product product);
    List<StorageUnit> findCompatibleStorage(Product product);

    List<StorageUnit> getAvailableUnits();
    void generateStorageReport();
}
