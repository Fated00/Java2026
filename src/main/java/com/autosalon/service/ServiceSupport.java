package com.autosalon.service;

import com.autosalon.domain.Identifiable;
import com.autosalon.domain.enums.Role;
import com.autosalon.domain.exception.DomainValidationException;
import com.autosalon.domain.exception.EntityNotFoundException;
import com.autosalon.domain.model.User;
import com.autosalon.repository.CrudRepository;

import java.util.UUID;

import static com.autosalon.domain.validation.DomainValidator.requireNonNull;

final class ServiceSupport {
    private ServiceSupport() {
    }

    static <T extends Identifiable> T findOrThrow(CrudRepository<T> repository, UUID id, String entityName) {
        requireNonNull(repository, "repository");
        return repository.findById(id)
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
