package com.warehouse.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Triggers reorder alerts when stock hits critical levels.
 * Demonstrates: Observer reacting to specific event types
 */
public class ReorderAlertObserver implements StockObserver {

    private final int reorderThreshold;
    private final List<String> pendingReorders = new ArrayList<>();

    public ReorderAlertObserver(int reorderThreshold) {
        this.reorderThreshold = reorderThreshold;
    }

    @Override
    public void onStockEvent(StockEvent event) {
        if (event.getType() == StockEvent.EventType.LOW_STOCK
                || event.getType() == StockEvent.EventType.OUT_OF_STOCK) {

            if (!pendingReorders.contains(event.getProductId())) {
                pendingReorders.add(event.getProductId());
                System.out.printf("[REORDER ALERT] Product '%s' needs restocking! " +
                        "Current qty: %d (threshold: %d)%n",
                        event.getProductName(), event.getCurrentQuantity(), reorderThreshold);
            }
        }
    }

    public List<String> getPendingReorders() {
        return new ArrayList<>(pendingReorders);
    }

    public void clearReorder(String productId) {
        pendingReorders.remove(productId);
    }
}
