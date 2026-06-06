package com.autosalon.repository;

import com.autosalon.domain.Identifiable;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.autosalon.domain.validation.DomainValidator.requireId;
import static com.autosalon.domain.validation.DomainValidator.requireNonNull;

public final class InMemoryCrudRepository<T extends Identifiable> implements CrudRepository<T> {
    private final Map<UUID, T> storage = new ConcurrentHashMap<>();

    @Override
    public T save(T entity) {
        requireNonNull(entity, "entity");
        storage.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public Optional<T> findById(UUID id) {
        return Optional.ofNullable(storage.get(requireId(id, "entity id")));
    }

    @Override
    public List<T> findAll() {
        return storage.values().stream()
                .sorted(Comparator.comparing(entity -> entity.getId().toString()))
                .toList();
    }

    @Override
    public boolean existsById(UUID id) {
        return storage.containsKey(requireId(id, "entity id"));
    }

    @Override
    public void deleteById(UUID id) {
        storage.remove(requireId(id, "entity id"));
    }

    @Override
    public void deleteAll() {
        storage.clear();
    }
}
