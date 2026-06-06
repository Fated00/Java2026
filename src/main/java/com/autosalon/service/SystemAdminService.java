package com.autosalon.service;

import com.autosalon.domain.Identifiable;
import com.autosalon.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

import static com.autosalon.domain.validation.DomainValidator.requireNonNull;

public final class SystemAdminService {
    public <T extends Identifiable> T create(CrudRepository<T> repository, T entity) {
        return requireNonNull(repository, "repository").save(requireNonNull(entity, "entity"));
    }

    public <T extends Identifiable> T update(CrudRepository<T> repository, T entity) {
        return requireNonNull(repository, "repository").save(requireNonNull(entity, "entity"));
    }

    public <T extends Identifiable> T view(CrudRepository<T> repository, UUID id, String entityName) {
        return ServiceSupport.findOrThrow(requireNonNull(repository, "repository"), id, entityName);
    }

    public <T extends Identifiable> List<T> list(CrudRepository<T> repository) {
        return requireNonNull(repository, "repository").findAll();
    }

    public <T extends Identifiable> void delete(CrudRepository<T> repository, UUID id) {
        CrudRepository<T> checkedRepository = requireNonNull(repository, "repository");
        ServiceSupport.findOrThrow(checkedRepository, id, "Entity");
        checkedRepository.deleteById(id);
    }
}
