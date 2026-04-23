package com.warehouse.repository;

import com.warehouse.model.product.Product;
import java.util.List;

/**
 * Product-specific repository with domain queries.
 * Demonstrates: Interface extension, Single Responsibility (data access only)
 */
public interface ProductRepository extends Repository<Product, String> {

    List<Product> findBySku(String sku);

    List<Product> findBySupplierId(String supplierId);

    List<Product> findByProductType(String productType);

    List<Product> findLowStock(int threshold);

    List<Product> findOutOfStock();
}
