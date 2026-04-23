package com.warehouse.model.storage;

import com.warehouse.model.product.BulkProduct;
import com.warehouse.model.product.Product;

/**
 * Hazmat vault - implements HazmatCapable.
 * Demonstrates: ISP - only this class needs HazmatCapable, others are unaffected
 */
public class HazmatVault extends AbstractStorageUnit implements HazmatCapable {

    private final boolean fireSuppression;
    private final boolean ventilation;
    private final boolean leakDetection;
    private boolean isLocked;

    public HazmatVault(String id, String name, String location, int capacity,
                       boolean fireSuppression, boolean ventilation, boolean leakDetection) {
        super(id, name, location, capacity);
        this.fireSuppression = fireSuppression;
        this.ventilation = ventilation;
        this.leakDetection = leakDetection;
        this.isLocked = true;
    }

    /**
     * Only stores hazardous or flammable bulk products.
     * And only if vault is compliant.
     */
    @Override
    public boolean canStore(Product product) {
        if (!isHazmatCompliant()) {
            return false; // Non-compliant vault cannot accept any products
        }
        if (product instanceof BulkProduct bulk) {
            return bulk.isHazardous() || bulk.isFlammable();
        }
        return false;
    }

    @Override
    public String getStorageType() {
        return "HAZMAT_VAULT";
    }

    @Override
    public boolean hasFireSuppression() { return fireSuppression; }

    @Override
    public boolean hasVentilation() { return ventilation; }

    @Override
    public boolean hasLeakDetection() { return leakDetection; }

    public boolean isLocked() { return isLocked; }

    public void lock() { this.isLocked = true; }
    public void unlock() { this.isLocked = false; }

    @Override
    public String toString() {
        return super.toString() + " | " + getComplianceStatus();
    }
}
