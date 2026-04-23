package com.warehouse.observer;

/**
 * Observer interface for stock events.
 * Demonstrates: Observer Pattern
 */
public interface StockObserver {
    void onStockEvent(StockEvent event);
}
