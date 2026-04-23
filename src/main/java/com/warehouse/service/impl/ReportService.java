package com.warehouse.service.impl;

import com.warehouse.model.product.PerishableProduct;
import com.warehouse.model.product.Product;
import com.warehouse.repository.ProductRepository;

import java.util.List;

/**
 * Report generation service.
 * Demonstrates: Single Responsibility — only reporting, no mutation of state.
 * Uses Polymorphism to handle different product types in a unified loop.
 */
public class ReportService {

    private final ProductRepository productRepository;
    private final int lowStockThreshold;
    private final int nearExpiryDays;

    public ReportService(ProductRepository productRepository,
                         int lowStockThreshold, int nearExpiryDays) {
        this.productRepository = productRepository;
        this.lowStockThreshold = lowStockThreshold;
        this.nearExpiryDays = nearExpiryDays;
    }

    public void generateFullInventoryReport() {
        List<Product> all = productRepository.findAll();

        System.out.println("\n" + "=".repeat(70));
        System.out.println("                    FULL INVENTORY REPORT");
        System.out.println("=".repeat(70));

        if (all.isEmpty()) {
            System.out.println("  No products in inventory.");
        }

        double totalValue = 0;
        for (Product p : all) {
            System.out.println("\n  " + p);
            System.out.println("    Storage requirement : " + p.getStorageRequirements());
            System.out.printf ("    Storage cost/unit   : $%.2f%n", p.calculateStorageCost());
            System.out.printf ("    Total value         : $%.2f%n", p.getPrice() * p.getQuantity());

            // Polymorphism in action — extra info for perishables
            if (p instanceof PerishableProduct pp) {
                System.out.printf("    Expiry              : %s (%d days remaining)%n",
                        pp.getExpiryDate(), pp.getDaysUntilExpiry());
                if (pp.isExpired()) {
                    System.out.println("    *** EXPIRED — Remove immediately ***");
                } else if (pp.isNearExpiry(nearExpiryDays)) {
                    System.out.println("    *** NEAR EXPIRY — Prioritize dispatch ***");
                }
            }

            totalValue += p.getPrice() * p.getQuantity();
        }

        System.out.println("\n" + "-".repeat(70));
        System.out.printf("  Total products  : %d%n", all.size());
        System.out.printf("  Total inv value : $%.2f%n", totalValue);
        System.out.println("=".repeat(70) + "\n");
    }

    public void generateLowStockReport() {
        List<Product> lowStock = productRepository.findLowStock(lowStockThreshold);
        List<Product> outOfStock = productRepository.findOutOfStock();

        System.out.println("\n" + "=".repeat(70));
        System.out.println("                    STOCK ALERT REPORT");
        System.out.println("=".repeat(70));

        System.out.println("\n  OUT OF STOCK (" + outOfStock.size() + "):");
        if (outOfStock.isEmpty()) {
            System.out.println("    None");
        } else {
            outOfStock.forEach(p -> System.out.println("    [CRITICAL] " + p.getName() + " | SKU: " + p.getSku()));
        }

        System.out.println("\n  LOW STOCK (threshold: " + lowStockThreshold + ") — " + lowStock.size() + " items:");
        if (lowStock.isEmpty()) {
            System.out.println("    None");
        } else {
            lowStock.stream()
                    .filter(p -> !p.isOutOfStock())
                    .forEach(p -> System.out.printf("    [WARNING] %s | SKU: %s | Qty: %d%n",
                            p.getName(), p.getSku(), p.getQuantity()));
        }

        System.out.println("\n" + "=".repeat(70) + "\n");
    }

    public void generateExpiryReport() {
        List<Product> perishables = productRepository.findByProductType("PERISHABLE");

        System.out.println("\n" + "=".repeat(70));
        System.out.println("                    EXPIRY REPORT");
        System.out.println("=".repeat(70));

        boolean anyIssues = false;
        for (Product p : perishables) {
            if (p instanceof PerishableProduct pp) {
                if (pp.isExpired()) {
                    System.out.printf("  [EXPIRED]     %s | Expired on: %s%n", pp.getName(), pp.getExpiryDate());
                    anyIssues = true;
                } else if (pp.isNearExpiry(nearExpiryDays)) {
                    System.out.printf("  [NEAR EXPIRY] %s | Expires in %d days (%s)%n",
                            pp.getName(), pp.getDaysUntilExpiry(), pp.getExpiryDate());
                    anyIssues = true;
                }
            }
        }

        if (!anyIssues) {
            System.out.println("  All perishable products are within safe expiry window.");
        }

        System.out.println("=".repeat(70) + "\n");
    }
}
