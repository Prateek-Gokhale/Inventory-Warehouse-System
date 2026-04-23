package com.warehouse.repository.impl;

import com.warehouse.model.storage.StorageUnit;
import com.warehouse.repository.StorageRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory implementation of StorageRepository.
 */
public class InMemoryStorageRepository implements StorageRepository {

    private final Map<String, StorageUnit> store = new LinkedHashMap<>();

    @Override
    public StorageUnit save(StorageUnit unit) {
        store.put(unit.getId(), unit);
        return unit;
    }

    @Override
    public Optional<StorageUnit> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<StorageUnit> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(String id) {
        store.remove(id);
    }

    @Override
    public boolean existsById(String id) {
        return store.containsKey(id);
    }

    @Override
    public int count() {
        return store.size();
    }

    @Override
    public List<StorageUnit> findByStorageType(String storageType) {
        return store.values().stream()
                .filter(u -> u.getStorageType().equalsIgnoreCase(storageType))
                .collect(Collectors.toList());
    }

    @Override
    public List<StorageUnit> findAvailableUnits() {
        return store.values().stream()
                .filter(u -> !u.isFull())
                .collect(Collectors.toList());
    }

    @Override
    public List<StorageUnit> findByLocation(String zone) {
        return store.values().stream()
                .filter(u -> u.getLocation().toLowerCase().contains(zone.toLowerCase()))
                .collect(Collectors.toList());
    }
}
