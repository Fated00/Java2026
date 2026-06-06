package com.autosalon.domain.model;

import com.autosalon.domain.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.util.UUID;

import static com.autosalon.domain.validation.DomainValidator.requireId;
import static com.autosalon.domain.validation.DomainValidator.requireNonNull;
import static com.autosalon.domain.validation.DomainValidator.requireText;

@Entity
@Table(name = "app_users")
public class User extends BaseEntity {
    @Column(nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    protected User() {
    }

    public User(UUID id, String fullName, Role role) {
        super(requireId(id, "user id"));
        this.fullName = requireText(fullName, "full name");
        this.role = requireNonNull(role, "role");
    }

    public static User create(String fullName, Role role) {
        return new User(UUID.randomUUID(), fullName, role);
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }
}
