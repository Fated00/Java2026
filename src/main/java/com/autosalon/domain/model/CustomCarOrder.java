package com.autosalon.domain.model;

import com.autosalon.domain.Identifiable;
import com.autosalon.domain.enums.CustomOrderStatus;
import com.autosalon.domain.enums.Role;
import com.autosalon.domain.exception.DomainValidationException;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.autosalon.domain.validation.DomainValidator.requireId;
import static com.autosalon.domain.validation.DomainValidator.requireNonNull;

public final class CustomCarOrder implements Identifiable {
    private final UUID id;
    private final User client;
    private final User manager;
    private final Configuration configuration;
    private final LocalDateTime createdAt;
    private CustomOrderStatus status;

    public CustomCarOrder(UUID id, User client, User manager, Configuration configuration) {
        this.id = requireId(id, "custom order id");
        this.client = requireUserRole(client, Role.CLIENT, "client");
        this.manager = requireUserRole(manager, Role.DEALERSHIP_MANAGER, "manager");
        this.configuration = requireNonNull(configuration, "configuration");
        this.createdAt = LocalDateTime.now();
        this.status = CustomOrderStatus.CREATED;
    }

    public static CustomCarOrder create(User client, User manager, Configuration configuration) {
        return new CustomCarOrder(UUID.randomUUID(), client, manager, configuration);
    }

    private static User requireUserRole(User user, Role expectedRole, String fieldName) {
        requireNonNull(user, fieldName);
        if (user.getRole() != expectedRole) {
            throw new DomainValidationException(fieldName + " must have role " + expectedRole);
        }
        return user;
    }

    @Override
    public UUID getId() {
        return id;
    }

    public User getClient() {
        return client;
    }

    public User getManager() {
        return manager;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public CustomOrderStatus getStatus() {
        return status;
    }

    public void setStatus(CustomOrderStatus status) {
        this.status = requireNonNull(status, "custom order status");
    }
}
