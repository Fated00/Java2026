package com.autosalon.domain.model;

import com.autosalon.domain.Identifiable;
import com.autosalon.domain.enums.InStockOrderStatus;
import com.autosalon.domain.enums.Role;
import com.autosalon.domain.exception.DomainValidationException;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.autosalon.domain.validation.DomainValidator.requireId;
import static com.autosalon.domain.validation.DomainValidator.requireNonNull;

public final class InStockCarOrder implements Identifiable {
    private final UUID id;
    private final User client;
    private final User manager;
    private final Car car;
    private final LocalDateTime createdAt;
    private InStockOrderStatus status;

    public InStockCarOrder(UUID id, User client, User manager, Car car) {
        this.id = requireId(id, "order id");
        this.client = requireUserRole(client, Role.CLIENT, "client");
        this.manager = requireUserRole(manager, Role.DEALERSHIP_MANAGER, "manager");
        this.car = requireNonNull(car, "car");
        this.createdAt = LocalDateTime.now();
        this.status = InStockOrderStatus.CREATED;
    }

    public static InStockCarOrder create(User client, User manager, Car car) {
        return new InStockCarOrder(UUID.randomUUID(), client, manager, car);
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

    public Car getCar() {
        return car;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public InStockOrderStatus getStatus() {
        return status;
    }

    public void setStatus(InStockOrderStatus status) {
        this.status = requireNonNull(status, "order status");
    }
}
