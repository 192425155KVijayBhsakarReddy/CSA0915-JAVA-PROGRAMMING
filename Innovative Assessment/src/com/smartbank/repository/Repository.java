package com.smartbank.repository;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Generic repository interface for CRUD operations.
 * Demonstrates Generics type parameters ID and T.
 */
public interface Repository<ID, T> extends Iterable<T> {
    T save(ID id, T entity);

    Optional<T> findById(ID id);

    List<T> findAll();

    boolean existsById(ID id);

    boolean deleteById(ID id);

    int count();

    void clear();

    @Override
    Iterator<T> iterator();
}
