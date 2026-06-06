package com.autosalon.service;

import com.autosalon.domain.model.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.autosalon.domain.validation.DomainValidator.requireNonNull;

@Service
@Transactional
public class SystemAdminService {
    public <T extends BaseEntity> T create(JpaRepository<T, UUID> repository, T entity) {
        return requireNonNull(repository, "repository").save(requireNonNull(entity, "entity"));
    }

    public <T extends BaseEntity> T update(JpaRepository<T, UUID> repository, T entity) {
        return requireNonNull(repository, "repository").save(requireNonNull(entity, "entity"));
    }

    @Transactional(readOnly = true)
    public <T extends BaseEntity> T view(JpaRepository<T, UUID> repository, UUID id, String entityName) {
        return ServiceSupport.findActiveOrThrow(requireNonNull(repository, "repository"), id, entityName);
    }

    @Transactional(readOnly = true)
    public <T extends BaseEntity> List<T> list(JpaRepository<T, UUID> repository) {
        return requireNonNull(repository, "repository").findAll().stream()
                .filter(entity -> !entity.isRemoved())
                .toList();
    }

    public <T extends BaseEntity> void delete(JpaRepository<T, UUID> repository, UUID id) {
        JpaRepository<T, UUID> checkedRepository = requireNonNull(repository, "repository");
        T entity = ServiceSupport.findActiveOrThrow(checkedRepository, id, "Entity");
        entity.markRemoved();
        checkedRepository.save(entity);
    }
}
