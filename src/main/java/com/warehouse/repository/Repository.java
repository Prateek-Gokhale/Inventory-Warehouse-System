package com.warehouse.repository;

import java.util.List;
import java.util.Optional;

/**
 * Generic repository interface.
 * Demonstrates: Repository Pattern, Dependency Inversion (SOLID)
 *
 * Service layer depends on this interface, NOT on concrete implementations.
 * This means we can swap InMemoryProductRepository for a DatabaseProductRepository
 * without changing any service code.
 *
 * @param <T>  The entity type
 * @param <ID> The ID type
 */
public interface Repository<T, ID> {

    T save(T entity);

    Optional<T> findById(ID id);

    List<T> findAll();

    void deleteById(ID id);

    boolean existsById(ID id);

    int count();
}
