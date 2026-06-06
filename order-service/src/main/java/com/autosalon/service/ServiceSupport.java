package com.autosalon.service;

import com.autosalon.domain.enums.Role;
import com.autosalon.domain.exception.DomainValidationException;
import com.autosalon.domain.exception.EntityNotFoundException;
import com.autosalon.domain.model.BaseEntity;
import com.autosalon.domain.model.User;
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

    static User requireRole(User user, Role role, String fieldName) {
        requireNonNull(user, fieldName);
        if (user.getRole() != role) {
            throw new DomainValidationException(fieldName + " must have role " + role);
        }
        return user;
    }
}
