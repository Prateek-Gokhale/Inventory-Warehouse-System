package com.warehouse.model.storage;

import com.warehouse.model.product.BulkProduct;
import com.warehouse.model.product.ElectronicProduct;
import com.warehouse.model.product.PerishableProduct;
import com.warehouse.model.product.Product;

/**
 * Standard shelf - no special capabilities.
 * Demonstrates: Concrete class, ISP (does NOT implement TemperatureControlled or HazmatCapable)
 */
public class Shelf extends AbstractStorageUnit {

    private final boolean isAntiStatic;
    private final boolean isPadded;

    public Shelf(String id, String name, String location, int capacity,
                 boolean isAntiStatic, boolean isPadded) {
        super(id, name, location, capacity);
        this.isAntiStatic = isAntiStatic;
        this.isPadded = isPadded;
    }

    /**
     * Shelf cannot store: hazardous/flammable bulk, perishables needing refrigeration,
     * or electronics needing anti-static unless it has that capability.
     */
    @Override
    public boolean canStore(Product product) {
        if (product instanceof PerishableProduct perishable) {
            return !perishable.isRequiresRefrigeration();
        }
        if (product instanceof BulkProduct bulk) {
            return !bulk.isHazardous() && !bulk.isFlammable();
        }
        if (product instanceof ElectronicProduct electronic) {
            if (electronic.isRequiresAntiStaticStorage() && !isAntiStatic) return false;
            if (electronic.isFragile() && !isPadded) return false;
            return true;
        }
        return true;
    }

    @Override
    public String getStorageType() {
        return "SHELF";
    }

    public boolean isAntiStatic() { return isAntiStatic; }
    public boolean isPadded() { return isPadded; }
}
