package com.warehouse;

import com.warehouse.config.WarehouseConfig;
import com.warehouse.factory.ProductFactory;
import com.warehouse.factory.StorageUnitFactory;
import com.warehouse.model.product.*;
import com.warehouse.model.storage.*;
import com.warehouse.observer.ReorderAlertObserver;
import com.warehouse.observer.StockEventLogger;
import com.warehouse.repository.impl.InMemoryProductRepository;
import com.warehouse.repository.impl.InMemoryStorageRepository;
import com.warehouse.service.impl.InventoryServiceImpl;
import com.warehouse.service.impl.ReportService;
import com.warehouse.service.impl.StorageServiceImpl;

import java.time.LocalDate;
import java.util.Map;

/**
 * Main entry point — demonstrates the full system end to end.
 *
 * What you'll see:
 *  1. Singleton config setup
 *  2. All 3 product types created (via Factory Pattern)
 *  3. All 3 storage unit types created (via Factory Pattern)
 *  4. Observer pattern firing on stock changes
 *  5. Polymorphism: canStore() rejecting wrong product-storage combos
 *  6. Reports: inventory, low stock, expiry
 */
public class WarehouseApp {

    public static void main(String[] args) {

        // ----------------------------------------------------------------
        // 1. Singleton configuration
        // ----------------------------------------------------------------
        WarehouseConfig config = WarehouseConfig.getInstance();
        config.setWarehouseName("Mumbai Central Warehouse");
        config.setDefaultLowStockThreshold(10);
        config.setNearExpiryDaysThreshold(30);
        System.out.println("Config loaded: " + config);

        // ----------------------------------------------------------------
        // 2. Wire up dependencies (manual DI — no framework)
        // ----------------------------------------------------------------
        var productRepo  = new InMemoryProductRepository();
        var storageRepo  = new InMemoryStorageRepository();

        var inventoryService = new InventoryServiceImpl(productRepo, config.getDefaultLowStockThreshold());
        var storageService   = new StorageServiceImpl(storageRepo, productRepo);
        var reportService    = new ReportService(productRepo,
                config.getDefaultLowStockThreshold(),
                config.getNearExpiryDaysThreshold());

        // ----------------------------------------------------------------
        // 3. Register observers
        // ----------------------------------------------------------------
        var logger        = new StockEventLogger();
        var reorderAlert  = new ReorderAlertObserver(config.getDefaultLowStockThreshold());

        inventoryService.registerObserver(logger);
        inventoryService.registerObserver(reorderAlert);
        storageService.registerObserver(logger);

        System.out.println("\n--- Observers registered ---\n");

        // ----------------------------------------------------------------
        // 4. Create products using Factory Pattern
        // ----------------------------------------------------------------
        System.out.println("=== Creating Products ===");

        // Perishable: milk
        PerishableProduct milk = new PerishableProduct(
                "Full Cream Milk", "DAIRY-001", 55.0, 200,
                "SUP-DAIRY-01",
                LocalDate.now().plusDays(10),
                4.0
        );

        // Perishable: near-expiry bread
        PerishableProduct bread = new PerishableProduct(
                "Whole Wheat Bread", "BAKERY-001", 45.0, 8,
                "SUP-BAKERY-01",
                LocalDate.now().plusDays(3),
                18.0
        );

        // Electronic: laptop
        ElectronicProduct laptop = new ElectronicProduct(
                "ThinkPad X1 Carbon", "ELEC-LAP-001", 85000.0, 15,
                "SUP-ELEC-01",
                "Lenovo", "X1C-Gen11", 24,
                true, true, 1.12
        );

        // Electronic: USB cable (non-fragile, no anti-static)
        ElectronicProduct usbCable = new ElectronicProduct(
                "USB-C Cable 2m", "ELEC-ACC-001", 299.0, 500,
                "SUP-ELEC-01",
                "Anker", "A8856", 12,
                false, false, 0.1
        );

        // Bulk: industrial solvent (hazardous + flammable)
        BulkProduct solvent = new BulkProduct(
                "Industrial Acetone", "BULK-CHM-001", 120.0, 50,
                "SUP-CHEM-01",
                BulkProduct.Unit.LITER, 20.0,
                true, true, 2.0
        );

        // Bulk: rice (non-hazardous)
        BulkProduct rice = new BulkProduct(
                "Basmati Rice 25kg", "BULK-FOOD-001", 1800.0, 100,
                "SUP-FOOD-01",
                BulkProduct.Unit.KG, 25.0,
                false, false, 3.0
        );

        // Register all products
        inventoryService.addProduct(milk);
        inventoryService.addProduct(bread);
        inventoryService.addProduct(laptop);
        inventoryService.addProduct(usbCable);
        inventoryService.addProduct(solvent);
        inventoryService.addProduct(rice);

        // ----------------------------------------------------------------
        // 5. Create storage units using Factory Pattern
        // ----------------------------------------------------------------
        System.out.println("\n=== Creating Storage Units ===");

        Shelf standardShelf   = StorageUnitFactory.createStandardShelf  ("SH-01", "Standard Shelf A1", "Zone-A, Row-1", 1000);
        Shelf antiStaticShelf = StorageUnitFactory.createAntiStaticShelf ("SH-02", "Anti-Static Shelf B1", "Zone-B, Row-1", 200);
        Freezer coldRoom      = StorageUnitFactory.createColdRoom        ("FR-01", "Cold Room C1", "Zone-C, Row-1", 500);
        Freezer deepFreezer   = StorageUnitFactory.createFreezer         ("FR-02", "Deep Freezer C2", "Zone-C, Row-2", 100);
        HazmatVault vault     = StorageUnitFactory.createFullyEquippedVault("HV-01", "Hazmat Vault D1", "Zone-D, Row-1", 200);

        storageService.addStorageUnit(standardShelf);
        storageService.addStorageUnit(antiStaticShelf);
        storageService.addStorageUnit(coldRoom);
        storageService.addStorageUnit(deepFreezer);
        storageService.addStorageUnit(vault);

        System.out.println("Storage units registered:");
        storageService.getAllStorageUnits().forEach(u -> System.out.println("  " + u));

        // ----------------------------------------------------------------
        // 6. Store products — Polymorphism via canStore()
        // ----------------------------------------------------------------
        System.out.println("\n=== Storing Products ===");

        // Milk → Cold Room (needs refrigeration at 4°C)
        storageService.storeProduct(milk.getId(), coldRoom.getId(), 200);
        System.out.println("Stored milk in cold room.");

        // Bread → Standard Shelf (no refrigeration needed)
        storageService.storeProduct(bread.getId(), standardShelf.getId(), 8);
        System.out.println("Stored bread on standard shelf.");

        // Laptop → Anti-Static shelf (needs anti-static + padded)
        storageService.storeProduct(laptop.getId(), antiStaticShelf.getId(), 15);
        System.out.println("Stored laptops on anti-static shelf.");

        // USB Cable → Standard shelf (no special needs)
        storageService.storeProduct(usbCable.getId(), standardShelf.getId(), 500);
        System.out.println("Stored USB cables on standard shelf.");

        // Solvent → Hazmat Vault
        storageService.storeProduct(solvent.getId(), vault.getId(), 50);
        System.out.println("Stored solvent in hazmat vault.");

        // Rice → Standard shelf
        storageService.storeProduct(rice.getId(), standardShelf.getId(), 100);
        System.out.println("Stored rice on standard shelf.");

        // ----------------------------------------------------------------
        // 7. Demonstrate ISP: trying wrong storage
        // ----------------------------------------------------------------
        System.out.println("\n=== Demonstrating ISP / Storage Validation ===");

        System.out.println("Attempting to store milk on standard shelf (wrong storage)...");
        try {
            storageService.storeProduct(milk.getId(), standardShelf.getId(), 10);
        } catch (Exception e) {
            System.out.println("  Correctly rejected: " + e.getMessage());
        }

        System.out.println("Attempting to store solvent on standard shelf...");
        try {
            storageService.storeProduct(solvent.getId(), standardShelf.getId(), 5);
        } catch (Exception e) {
            System.out.println("  Correctly rejected: " + e.getMessage());
        }

        System.out.println("Attempting to store laptop on standard shelf (no anti-static)...");
        try {
            storageService.storeProduct(laptop.getId(), standardShelf.getId(), 1);
        } catch (Exception e) {
            System.out.println("  Correctly rejected: " + e.getMessage());
        }

        // ----------------------------------------------------------------
        // 8. Observer: trigger low-stock and out-of-stock events
        // ----------------------------------------------------------------
        System.out.println("\n=== Triggering Stock Events (Observer Pattern) ===");

        // Dispatch bread until low stock triggers
        System.out.println("Dispatching 5 units of bread (8 → 3)...");
        inventoryService.dispatchStock(bread.getId(), 5);

        // Dispatch rest of bread → out of stock
        System.out.println("Dispatching remaining 3 units of bread (3 → 0)...");
        inventoryService.dispatchStock(bread.getId(), 3);

        // ----------------------------------------------------------------
        // 9. Generate reports
        // ----------------------------------------------------------------
        reportService.generateFullInventoryReport();
        reportService.generateLowStockReport();
        reportService.generateExpiryReport();
        storageService.generateStorageReport();

        // ----------------------------------------------------------------
        // 10. Show pending reorders
        // ----------------------------------------------------------------
        System.out.println("Pending reorder list: " + reorderAlert.getPendingReorders());
    }
}
