package com.warehouse.model.storage;

/**
 * Separate interface for hazardous material storage.
 * Demonstrates: Interface Segregation Principle (SOLID)
 *
 * Only HazmatVault implements this — Shelf and Freezer do NOT.
 */
public interface HazmatCapable {

    boolean hasFireSuppression();
    boolean hasVentilation();
    boolean hasLeakDetection();

    default boolean isHazmatCompliant() {
        return hasFireSuppression() && hasVentilation() && hasLeakDetection();
    }

    default String getComplianceStatus() {
        StringBuilder sb = new StringBuilder("Hazmat Compliance: ");
        sb.append(isHazmatCompliant() ? "COMPLIANT" : "NON-COMPLIANT");
        sb.append(" | Fire Suppression: ").append(hasFireSuppression() ? "✓" : "✗");
        sb.append(" | Ventilation: ").append(hasVentilation() ? "✓" : "✗");
        sb.append(" | Leak Detection: ").append(hasLeakDetection() ? "✓" : "✗");
        return sb.toString();
    }
}
