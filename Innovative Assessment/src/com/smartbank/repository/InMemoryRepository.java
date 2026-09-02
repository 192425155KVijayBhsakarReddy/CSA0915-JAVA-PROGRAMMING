package com.smartbank.repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Thread-safe generic repository implementation using ConcurrentHashMap and ArrayList.
 */
public class InMemoryRepository<ID, T> implements Repository<ID, T>, Serializable {
    private static final long serialVersionUID = 1L;

    protected final ConcurrentMap<ID, T> storage;

    public InMemoryRepository() {
        this.storage = new ConcurrentHashMap<>();
    }

    @Override
    public T save(ID id, T entity) {
        Objects.requireNonNull(id, "ID cannot be null");
        Objects.requireNonNull(entity, "Entity cannot be null");
        storage.put(id, entity);
        return entity;
    }

    @Override
    public Optional<T> findById(ID id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public boolean existsById(ID id) {
        if (id == null) return false;
        return storage.containsKey(id);
    }

    @Override
    public boolean deleteById(ID id) {
        if (id == null) return false;
        return storage.remove(id) != null;
    }

    @Override
    public int count() {
        return storage.size();
    }

    @Override
    public void clear() {
        storage.clear();
    }

    @Override
    public Iterator<T> iterator() {
        return storage.values().iterator();
    }
}
