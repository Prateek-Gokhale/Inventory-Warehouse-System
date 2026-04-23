package com.warehouse;

import com.warehouse.config.WarehouseConfig;
import com.warehouse.exception.InsufficientStockException;
import com.warehouse.exception.ProductNotFoundException;
import com.warehouse.exception.StorageException;
import com.warehouse.factory.StorageUnitFactory;
import com.warehouse.model.product.*;
import com.warehouse.model.storage.*;
import com.warehouse.observer.ReorderAlertObserver;
import com.warehouse.observer.StockEvent;
import com.warehouse.observer.StockEventLogger;
import com.warehouse.repository.impl.InMemoryProductRepository;
import com.warehouse.repository.impl.InMemoryStorageRepository;
import com.warehouse.service.impl.InventoryServiceImpl;
import com.warehouse.service.impl.StorageServiceImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Manual test suite — no JUnit dependency needed.
 * Run via main() to verify all core behaviours.
 */
public class WarehouseTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("Running Warehouse System Tests...\n");

        testSingletonConfig();
        testProductCreation();
        testInventoryAddAndGet();
        testReceiveAndDispatchStock();
        testInsufficientStockException();
        testProductNotFoundException();
        testStorageCompatibility();
        testStorageRejection();
        testObserverLowStock();
        testObserverOutOfStock();
        testPerishableExpiry();
        testLowStockQuery();

        System.out.println("\n" + "=".repeat(50));
        System.out.printf("Results: %d passed, %d failed%n", passed, failed);
        System.out.println("=".repeat(50));

        if (failed > 0) System.exit(1);
    }

    // ---- Tests ----

    static void testSingletonConfig() {
        WarehouseConfig a = WarehouseConfig.getInstance();
        WarehouseConfig b = WarehouseConfig.getInstance();
        assertTrue("Singleton returns same instance", a == b);
    }

    static void testProductCreation() {
        PerishableProduct p = new PerishableProduct(
                "Milk", "SKU-001", 55.0, 100, "SUP-01",
                LocalDate.now().plusDays(10), 4.0);
        assertTrue("Perishable type",           p.getProductType().equals("PERISHABLE"));
        assertTrue("Perishable requires refrig", p.isRequiresRefrigeration());
        assertTrue("Not expired",               !p.isExpired());

        ElectronicProduct e = new ElectronicProduct(
                "Laptop", "SKU-002", 80000.0, 5, "SUP-02",
                "Dell", "XPS-15", 24, true, true, 1.8);
        assertTrue("Electronic type",           e.getProductType().equals("ELECTRONIC"));
        assertTrue("Requires special storage",  e.requiresSpecialStorage());

        BulkProduct b = new BulkProduct(
                "Acetone", "SKU-003", 100.0, 20, "SUP-03",
                BulkProduct.Unit.LITER, 20.0, true, true, 2.0);
        assertTrue("Bulk type",                 b.getProductType().equals("BULK"));
        assertTrue("Bulk hazardous",            b.isHazardous());
    }

    static void testInventoryAddAndGet() {
        var repo    = new InMemoryProductRepository();
        var service = new InventoryServiceImpl(repo, 10);

        PerishableProduct p = new PerishableProduct(
                "Yogurt", "SKU-Y01", 40.0, 50, "SUP-01",
                LocalDate.now().plusDays(14), 4.0);

        service.addProduct(p);
        Product fetched = service.getProduct(p.getId());
        assertTrue("Product saved and retrieved", fetched.getName().equals("Yogurt"));
        assertTrue("Repo count is 1",             repo.count() == 1);
    }

    static void testReceiveAndDispatchStock() {
        var repo    = new InMemoryProductRepository();
        var service = new InventoryServiceImpl(repo, 5);

        ElectronicProduct e = new ElectronicProduct(
                "Phone", "SKU-P01", 20000.0, 30, "SUP-E01",
                "Samsung", "S24", 12, false, false, 0.2);
        service.addProduct(e);

        service.receiveStock(e.getId(), 20);
        assertTrue("Stock after receive = 50", service.getProduct(e.getId()).getQuantity() == 50);

        service.dispatchStock(e.getId(), 10);
        assertTrue("Stock after dispatch = 40", service.getProduct(e.getId()).getQuantity() == 40);
    }

    static void testInsufficientStockException() {
        var repo    = new InMemoryProductRepository();
        var service = new InventoryServiceImpl(repo, 5);

        BulkProduct rice = new BulkProduct("Rice", "SKU-R01", 1500.0, 10,
                "SUP-F01", BulkProduct.Unit.KG, 25.0, false, false, 3.0);
        service.addProduct(rice);

        try {
            service.dispatchStock(rice.getId(), 999);
            fail("Should have thrown InsufficientStockException");
        } catch (InsufficientStockException ex) {
            assertTrue("Correct exception type", true);
        }
    }

    static void testProductNotFoundException() {
        var repo    = new InMemoryProductRepository();
        var service = new InventoryServiceImpl(repo, 5);

        try {
            service.getProduct("non-existent-id");
            fail("Should have thrown ProductNotFoundException");
        } catch (ProductNotFoundException ex) {
            assertTrue("Correct exception for missing product", true);
        }
    }

    static void testStorageCompatibility() {
        Freezer coldRoom = StorageUnitFactory.createColdRoom("FR-01", "Cold Room", "Zone-C", 500);

        PerishableProduct milk = new PerishableProduct(
                "Milk", "SKU-M01", 55.0, 100, "SUP-01",
                LocalDate.now().plusDays(10), 4.0);

        assertTrue("Cold room can store refrigerated milk", coldRoom.canStore(milk));

        Shelf shelf = StorageUnitFactory.createStandardShelf("SH-01", "Shelf", "Zone-A", 500);
        assertTrue("Standard shelf cannot store refrigerated milk", !shelf.canStore(milk));
    }

    static void testStorageRejection() {
        var productRepo = new InMemoryProductRepository();
        var storageRepo = new InMemoryStorageRepository();
        var storageService = new StorageServiceImpl(storageRepo, productRepo);

        Shelf shelf = StorageUnitFactory.createStandardShelf("SH-01", "Shelf", "Zone-A", 500);
        storageService.addStorageUnit(shelf);

        PerishableProduct milk = new PerishableProduct(
                "Milk", "SKU-M01", 55.0, 50, "SUP-01",
                LocalDate.now().plusDays(10), 4.0);
        productRepo.save(milk);

        try {
            storageService.storeProduct(milk.getId(), shelf.getId(), 10);
            fail("Should have thrown StorageException");
        } catch (StorageException ex) {
            assertTrue("Storage correctly rejected incompatible product", true);
        }
    }

    static void testObserverLowStock() {
        var repo        = new InMemoryProductRepository();
        var service     = new InventoryServiceImpl(repo, 10);
        var reorderObs  = new ReorderAlertObserver(10);
        service.registerObserver(reorderObs);

        BulkProduct rice = new BulkProduct("Rice", "SKU-R01", 1500.0, 20,
                "SUP-F01", BulkProduct.Unit.KG, 25.0, false, false, 3.0);
        service.addProduct(rice);
        service.dispatchStock(rice.getId(), 13);  // 20 → 7, below threshold of 10

        assertTrue("Reorder list has rice",
                reorderObs.getPendingReorders().contains(rice.getId()));
    }

    static void testObserverOutOfStock() {
        var repo    = new InMemoryProductRepository();
        var service = new InventoryServiceImpl(repo, 5);

        AtomicReference<StockEvent.EventType> lastEvent = new AtomicReference<>();
        service.registerObserver(event -> lastEvent.set(event.getType()));

        ElectronicProduct item = new ElectronicProduct(
                "Earbuds", "SKU-E01", 1500.0, 5, "SUP-E01",
                "Boat", "Airdopes141", 6, false, false, 0.05);
        service.addProduct(item);
        service.dispatchStock(item.getId(), 5);  // → 0

        assertTrue("Observer received OUT_OF_STOCK event",
                lastEvent.get() == StockEvent.EventType.OUT_OF_STOCK);
    }

    static void testPerishableExpiry() {
        PerishableProduct expired = new PerishableProduct(
                "Old Milk", "SKU-EX01", 55.0, 10, "SUP-01",
                LocalDate.now().minusDays(1), 4.0);
        assertTrue("Product correctly identified as expired", expired.isExpired());

        PerishableProduct nearExpiry = new PerishableProduct(
                "Fresh Milk", "SKU-NE01", 55.0, 10, "SUP-01",
                LocalDate.now().plusDays(5), 4.0);
        assertTrue("Product correctly identified as near expiry", nearExpiry.isNearExpiry(7));
        assertTrue("Product correctly NOT identified as expired", !nearExpiry.isExpired());
    }

    static void testLowStockQuery() {
        var repo    = new InMemoryProductRepository();
        var service = new InventoryServiceImpl(repo, 10);

        ElectronicProduct high = new ElectronicProduct("TV", "SKU-TV01", 50000.0, 50,
                "SUP-E01", "LG", "OLED55", 24, false, false, 15.0);
        ElectronicProduct low  = new ElectronicProduct("Remote", "SKU-RM01", 500.0, 3,
                "SUP-E01", "LG", "AKB75", 6, false, false, 0.1);
        service.addProduct(high);
        service.addProduct(low);

        List<Product> lowStock = service.getLowStockProducts(10);
        assertTrue("Only low-stock product returned",
                lowStock.size() == 1 && lowStock.get(0).getName().equals("Remote"));
    }

    // ---- Assertion helpers ----

    static void assertTrue(String label, boolean condition) {
        if (condition) {
            System.out.println("  PASS: " + label);
            passed++;
        } else {
            System.out.println("  FAIL: " + label);
            failed++;
        }
    }

    static void fail(String label) {
        System.out.println("  FAIL: " + label + " (no exception thrown)");
        failed++;
    }
}
