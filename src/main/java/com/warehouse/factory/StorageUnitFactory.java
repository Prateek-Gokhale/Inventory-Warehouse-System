package com.warehouse.factory;

import com.warehouse.model.storage.*;

/**
 * Factory for creating storage units.
 * Demonstrates: Factory Pattern - encapsulates complex object creation
 */
public class StorageUnitFactory {

    public static Shelf createStandardShelf(String id, String name, String location, int capacity) {
        return new Shelf(id, name, location, capacity, false, false);
    }

    public static Shelf createAntiStaticShelf(String id, String name, String location, int capacity) {
        return new Shelf(id, name, location, capacity, true, true);
    }

    public static Freezer createFreezer(String id, String name, String location, int capacity) {
        // Standard freezer: -25°C to -10°C
        return new Freezer(id, name, location, capacity, -25.0, -10.0, -18.0);
    }

    public static Freezer createColdRoom(String id, String name, String location, int capacity) {
        // Cold room: 0°C to 8°C
        return new Freezer(id, name, location, capacity, 0.0, 8.0, 4.0);
    }

    public static HazmatVault createFullyEquippedVault(String id, String name, String location, int capacity) {
        return new HazmatVault(id, name, location, capacity, true, true, true);
    }

    public static HazmatVault createBasicVault(String id, String name, String location, int capacity) {
        // Missing leak detection — non-compliant
        return new HazmatVault(id, name, location, capacity, true, true, false);
    }
}
