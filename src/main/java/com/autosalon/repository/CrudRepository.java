package com.autosalon.repository;

import com.autosalon.domain.Identifiable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CrudRepository<T extends Identifiable> {
    T save(T entity);

    Optional<T> findById(UUID id);

    List<T> findAll();

    boolean existsById(UUID id);

    void deleteById(UUID id);

    void deleteAll();
}
