package com.autosalon.service;

import com.autosalon.domain.exception.EntityNotFoundException;
import com.autosalon.domain.model.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

import static com.autosalon.domain.validation.DomainValidator.requireNonNull;

final class ServiceSupport {
    private ServiceSupport() {
    }

    static <T extends BaseEntity> T findActiveOrThrow(JpaRepository<T, UUID> repository, UUID id, String entityName) {
        requireNonNull(repository, "repository");
        return repository.findById(id)
                .filter(entity -> !entity.isRemoved())
                .orElseThrow(() -> new EntityNotFoundException(entityName, id));
    }
}
