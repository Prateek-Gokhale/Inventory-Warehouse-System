package com.warehouse.observer;

/**
 * Logs all stock events to console/log.
 * In a real system, this would write to a logging framework or database.
 */
public class StockEventLogger implements StockObserver {

    private static final String RESET = "\u001B[0m";
    private static final String RED   = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN  = "\u001B[32m";
    private static final String CYAN   = "\u001B[36m";

    @Override
    public void onStockEvent(StockEvent event) {
        String color = switch (event.getType()) {
            case OUT_OF_STOCK, PRODUCT_EXPIRED, TEMPERATURE_ALERT -> RED;
            case LOW_STOCK, NEAR_EXPIRY, STORAGE_FULL -> YELLOW;
            case STOCK_ADDED -> GREEN;
            default -> CYAN;
        };
        System.out.println(color + "[LOG] " + event + RESET);
    }
}
