package com.warehouse.repository;

import com.warehouse.model.storage.StorageUnit;
import java.util.List;

/**
 * Storage unit repository interface.
 */
public interface StorageRepository extends Repository<StorageUnit, String> {

    List<StorageUnit> findByStorageType(String storageType);

    List<StorageUnit> findAvailableUnits();  // non-full units

    List<StorageUnit> findByLocation(String zone);
}
