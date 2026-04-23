# 📦 Inventory & Warehouse Management System

A backend Java application demonstrating **all four OOP pillars** through a real-world domain: tracking products, managing physical storage, firing stock alerts, and generating reports.

No external frameworks or databases. Pure Java 21 — designed to be read, understood, and extended.

---

## Table of Contents

- [Project Overview](#project-overview)
- [OOP Concepts Demonstrated](#oop-concepts-demonstrated)
- [Design Patterns Used](#design-patterns-used)
- [SOLID Principles](#solid-principles)
- [Project Structure](#project-structure)
- [Class Diagram](#class-diagram)
- [How to Run](#how-to-run)
- [Running Tests](#running-tests)
- [Sample Output](#sample-output)
- [Extending the System](#extending-the-system)

---

## Project Overview

The system manages three concerns:

| Concern | Responsibility |
|---|---|
| **Inventory** | Track products, quantities, stock levels |
| **Storage** | Assign products to physical storage units |
| **Alerts** | Notify observers when stock or storage events occur |

These three concerns map to three services (`InventoryService`, `StorageService`, `ReportService`) that depend on interfaces, not on each other's internals.

---

## OOP Concepts Demonstrated

### 1. Inheritance

```
Product (abstract)
├── PerishableProduct   — adds expiry date, temperature requirement
├── ElectronicProduct   — adds brand, warranty, fragility flags
└── BulkProduct         — adds unit type, hazard flags, stack height

AbstractStorageUnit (abstract, implements StorageUnit)
├── Shelf               — standard or anti-static/padded
├── Freezer             — temperature-controlled cold storage
└── HazmatVault         — hazardous material vault
```

Each subclass inherits shared state and overrides abstract methods to provide its own logic.

### 2. Polymorphism

The same method call behaves differently depending on the runtime type:

```java
// calculateStorageCost() on each product type
PerishableProduct milk   = ...  // cost increases as expiry approaches
ElectronicProduct laptop = ...  // cost based on weight + fragility
BulkProduct       rice   = ...  // cost based on unit size + hazard flags

// All called the same way — different behaviour each time
for (Product p : inventory) {
    double cost = p.calculateStorageCost();  // polymorphic dispatch
}

// canStore() on each storage unit type
Shelf      shelf  = ...  // rejects refrigerated or hazardous products
Freezer    fridge = ...  // only accepts refrigerated perishables in temp range
HazmatVault vault = ...  // only accepts hazardous/flammable bulk products

storageService.findBestStorageFor(product);  // works for any product type
```

### 3. Abstraction

Two layers of abstraction:

- **Abstract classes** (`Product`, `AbstractStorageUnit`) — define common state and shared behaviour, force subclasses to implement domain-specific methods
- **Interfaces** (`StorageUnit`, `TemperatureControlled`, `HazmatCapable`, `Repository`) — define contracts that callers depend on

### 4. Encapsulation

- All fields are `private`
- State changes go through methods that enforce business rules (`updateQuantity` validates negatives, `addProduct` validates capacity and compatibility)
- Internal collections are returned as unmodifiable copies

---

## Design Patterns Used

### Factory Pattern

`ProductFactory` and `StorageUnitFactory` encapsulate complex object creation.

```java
// Instead of spreading constructor logic across the codebase
Freezer coldRoom = StorageUnitFactory.createColdRoom("FR-01", "Cold Room C1", "Zone-C", 500);
HazmatVault vault = StorageUnitFactory.createFullyEquippedVault("HV-01", "Vault D1", "Zone-D", 200);
```

Adding a new product type requires only extending `ProductFactory` — no other changes.

### Observer Pattern

`InventoryServiceImpl` notifies all registered observers when stock changes.

```java
// Register any number of observers
inventoryService.registerObserver(new StockEventLogger());      // logs all events
inventoryService.registerObserver(new ReorderAlertObserver(10)); // triggers reorders

// When stock drops below threshold, all observers are notified automatically
inventoryService.dispatchStock(productId, quantity);
```

Observers are fully decoupled — adding a new one (e.g. `EmailNotifier`, `SlackAlerter`) requires zero changes to `InventoryServiceImpl`.

### Repository Pattern

Services depend on `ProductRepository` and `StorageRepository` interfaces.

```java
// InventoryServiceImpl constructor
public InventoryServiceImpl(ProductRepository productRepository, int lowStockThreshold) { ... }
```

Swap `InMemoryProductRepository` for a `PostgresProductRepository` — zero changes to service code.

### Singleton Pattern

`WarehouseConfig` uses the Singleton pattern to ensure a single shared configuration:

```java
WarehouseConfig config = WarehouseConfig.getInstance();
config.setDefaultLowStockThreshold(10);
```

---

## SOLID Principles

| Principle | Where Applied |
|---|---|
| **S** — Single Responsibility | `InventoryService` manages stock counts. `StorageService` manages physical placement. `ReportService` generates reports. None overlaps. |
| **O** — Open/Closed | Add a new product type by extending `Product` and updating `ProductFactory`. No existing class needs modification. |
| **L** — Liskov Substitution | Any `Product` subclass can be passed to `InventoryService`. Any `StorageUnit` implementation works in `StorageService`. |
| **I** — Interface Segregation | `Shelf` implements only `StorageUnit`. `Freezer` adds `TemperatureControlled`. `HazmatVault` adds `HazmatCapable`. No class implements methods it doesn't need. |
| **D** — Dependency Inversion | Services depend on `ProductRepository` (interface), not `InMemoryProductRepository` (concrete class). Wiring happens in `WarehouseApp`. |

---

## Project Structure

```
src/
├── main/java/com/warehouse/
│   ├── WarehouseApp.java                        ← Entry point / demo
│   ├── config/
│   │   └── WarehouseConfig.java                 ← Singleton config
│   ├── model/
│   │   ├── product/
│   │   │   ├── Product.java                     ← Abstract base
│   │   │   ├── PerishableProduct.java
│   │   │   ├── ElectronicProduct.java
│   │   │   └── BulkProduct.java
│   │   └── storage/
│   │       ├── StorageUnit.java                 ← Interface
│   │       ├── TemperatureControlled.java       ← Interface (ISP)
│   │       ├── HazmatCapable.java               ← Interface (ISP)
│   │       ├── AbstractStorageUnit.java         ← Abstract base
│   │       ├── Shelf.java
│   │       ├── Freezer.java
│   │       └── HazmatVault.java
│   ├── repository/
│   │   ├── Repository.java                      ← Generic interface
│   │   ├── ProductRepository.java
│   │   ├── StorageRepository.java
│   │   └── impl/
│   │       ├── InMemoryProductRepository.java
│   │       └── InMemoryStorageRepository.java
│   ├── service/
│   │   ├── InventoryService.java                ← Interface
│   │   ├── StorageService.java                  ← Interface
│   │   └── impl/
│   │       ├── InventoryServiceImpl.java
│   │       ├── StorageServiceImpl.java
│   │       └── ReportService.java
│   ├── observer/
│   │   ├── StockObserver.java                   ← Observer interface
│   │   ├── StockEvent.java                      ← Event value object
│   │   ├── StockEventLogger.java
│   │   └── ReorderAlertObserver.java
│   ├── factory/
│   │   ├── ProductFactory.java
│   │   └── StorageUnitFactory.java
│   └── exception/
│       ├── ProductNotFoundException.java
│       ├── InsufficientStockException.java
│       └── StorageException.java
└── test/java/com/warehouse/
    └── WarehouseTest.java                       ← 23 tests, no JUnit needed
```

---

## Class Diagram

```
                          ┌─────────────────┐
                          │    Product      │  ← abstract
                          │─────────────────│
                          │ - id, name, sku │
                          │ - price, qty    │
                          │─────────────────│
                          │ + getProductType│  ← abstract
                          │ + requiresSpec  │  ← abstract
                          │ + calcStorCost  │  ← abstract
                          └────────┬────────┘
               ┌──────────────────┬┴────────────────────┐
       ┌───────┴──────┐  ┌────────┴───────┐  ┌──────────┴──────┐
       │  Perishable  │  │  Electronic    │  │    Bulk         │
       │  Product     │  │  Product       │  │    Product      │
       └──────────────┘  └────────────────┘  └─────────────────┘

                          ┌─────────────────┐
                          │  StorageUnit    │  ← interface
                          └────────┬────────┘
                                   │
                          ┌────────┴────────┐
                          │ AbstractStorage │  ← abstract
                          │     Unit        │
                          └────────┬────────┘
          ┌────────────────────────┼───────────────────────┐
   ┌──────┴─────┐          ┌───────┴──────┐        ┌───────┴──────┐
   │   Shelf    │          │   Freezer    │        │ HazmatVault  │
   └────────────┘          │──────────────│        │──────────────│
                           │ implements   │        │ implements   │
                           │ Temperature  │        │ HazmatCapable│
                           │ Controlled   │        └──────────────┘
                           └──────────────┘

   InventoryServiceImpl ──→ ProductRepository (interface)
                                    ↑
                         InMemoryProductRepository

   InventoryServiceImpl ──→ StockObserver[] (interface)
                                    ↑
                    ┌───────────────┴───────────┐
             StockEventLogger         ReorderAlertObserver
```

---

## How to Run

**Requirements:** Java 21+

```bash
# Clone the repo
git clone https://github.com/yourusername/inventory-warehouse-system.git
cd inventory-warehouse-system

# Compile
mkdir -p out/main out/test

javac --enable-preview --release 21 \
  -d out/main \
  $(find src/main/java -name "*.java")

# Run the full demo
java --enable-preview -cp out/main com.warehouse.WarehouseApp
```

---

## Running Tests

```bash
# Compile tests
javac --enable-preview --release 21 \
  -cp out/main \
  -d out/test \
  $(find src/test/java -name "*.java")

# Run tests
java --enable-preview -cp out/main:out/test com.warehouse.WarehouseTest
```

Expected output:
```
Running Warehouse System Tests...

  PASS: Singleton returns same instance
  PASS: Perishable type
  ... (23 tests)

==================================================
Results: 23 passed, 0 failed
==================================================
```

---

## Sample Output (Demo App)

```
Config loaded: WarehouseConfig { name='Mumbai Central Warehouse', ... }

=== Triggering Stock Events (Observer Pattern) ===
Dispatching 5 units of bread (8 → 3)...
[LOG] LOW_STOCK | Product: Whole Wheat Bread | Qty: 3 | WARNING: Stock below threshold of 10
[REORDER ALERT] Product 'Whole Wheat Bread' needs restocking! Current qty: 3 (threshold: 10)

=== Demonstrating ISP / Storage Validation ===
Attempting to store milk on standard shelf...
  Correctly rejected: Storage unit 'Standard Shelf A1' is incompatible with product
  'Full Cream Milk'. Requirements: Refrigerated storage at 4.0°C or below

=== EXPIRY REPORT ===
  [NEAR EXPIRY] Full Cream Milk    | Expires in 10 days
  [NEAR EXPIRY] Whole Wheat Bread  | Expires in 3 days
```

---

## Extending the System

**Add a new product type (e.g. FurnitureProduct):**
1. Extend `Product` and implement the 4 abstract methods
2. Add `FURNITURE` to `ProductFactory.ProductType` enum
3. Add a `createFurniture()` case in `ProductFactory`
4. No other files need to change — this is Open/Closed in action

**Add a new storage type (e.g. ColdChainVehicle):**
1. Extend `AbstractStorageUnit` and implement `canStore()` + `getStorageType()`
2. Optionally implement `TemperatureControlled` if needed
3. Add a factory method in `StorageUnitFactory`

**Add a new observer (e.g. EmailNotifier):**
1. Implement `StockObserver`
2. Register it: `inventoryService.registerObserver(new EmailNotifier(smtpConfig))`
3. Zero changes to `InventoryServiceImpl`

**Swap to a real database:**
1. Write `PostgresProductRepository implements ProductRepository`
2. Change one line in `WarehouseApp` where the repository is wired
3. All service code stays unchanged — this is Dependency Inversion in action

---

## Tech Stack

| Item | Detail |
|---|---|
| Language | Java 21 |
| Build | Manual `javac` (no Maven/Gradle) |
| Testing | Manual test suite (no JUnit) |
| Database | In-memory (swap-ready via Repository Pattern) |
| Frameworks | None |
