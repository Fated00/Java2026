package com.autosalon.domain.model;

import com.autosalon.domain.Identifiable;
import com.autosalon.domain.enums.Role;

import java.util.UUID;

import static com.autosalon.domain.validation.DomainValidator.requireId;
import static com.autosalon.domain.validation.DomainValidator.requireNonNull;
import static com.autosalon.domain.validation.DomainValidator.requireText;

public final class User implements Identifiable {
    private final UUID id;
    private final String fullName;
    private final Role role;

    public User(UUID id, String fullName, Role role) {
        this.id = requireId(id, "user id");
        this.fullName = requireText(fullName, "full name");
        this.role = requireNonNull(role, "role");
    }

    public static User create(String fullName, Role role) {
        return new User(UUID.randomUUID(), fullName, role);
    }

    @Override
    public UUID getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }
}
