package com.warehouse.repository.impl;

import com.warehouse.model.product.Product;
import com.warehouse.repository.ProductRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory implementation of ProductRepository.
 * Demonstrates: Dependency Inversion (DI) - this is the concrete detail,
 * services depend on the ProductRepository abstraction, not this class.
 */
public class InMemoryProductRepository implements ProductRepository {

    private final Map<String, Product> store = new LinkedHashMap<>();

    @Override
    public Product save(Product product) {
        store.put(product.getId(), product);
        return product;
    }

    @Override
    public Optional<Product> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Product> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(String id) {
        if (!store.containsKey(id)) {
            throw new NoSuchElementException("Product not found: " + id);
        }
        store.remove(id);
    }

    @Override
    public boolean existsById(String id) {
        return store.containsKey(id);
    }

    @Override
    public int count() {
        return store.size();
    }

    @Override
    public List<Product> findBySku(String sku) {
        return store.values().stream()
                .filter(p -> p.getSku().equalsIgnoreCase(sku))
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> findBySupplierId(String supplierId) {
        return store.values().stream()
                .filter(p -> p.getSupplierId().equals(supplierId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> findByProductType(String productType) {
        return store.values().stream()
                .filter(p -> p.getProductType().equalsIgnoreCase(productType))
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> findLowStock(int threshold) {
        return store.values().stream()
                .filter(p -> p.isLowStock(threshold))
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> findOutOfStock() {
        return store.values().stream()
                .filter(Product::isOutOfStock)
                .collect(Collectors.toList());
    }
}
