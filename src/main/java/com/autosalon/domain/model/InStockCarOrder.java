package com.autosalon.domain.model;

import com.autosalon.domain.enums.InStockOrderStatus;
import com.autosalon.domain.enums.Role;
import com.autosalon.domain.exception.DomainValidationException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.autosalon.domain.validation.DomainValidator.requireId;
import static com.autosalon.domain.validation.DomainValidator.requireNonNull;

@Entity
@Table(name = "in_stock_car_orders")
public class InStockCarOrder extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(optional = false)
    @JoinColumn(name = "manager_id", nullable = false)
    private User manager;

    @ManyToOne(optional = false)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @Column(nullable = false)
    private LocalDateTime orderedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InStockOrderStatus status;

    protected InStockCarOrder() {
    }

    public InStockCarOrder(UUID id, User client, User manager, Car car) {
        super(requireId(id, "order id"));
        this.client = requireUserRole(client, Role.CLIENT, "client");
        this.manager = requireUserRole(manager, Role.DEALERSHIP_MANAGER, "manager");
        this.car = requireNonNull(car, "car");
        this.orderedAt = LocalDateTime.now();
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

    public User getClient() {
        return client;
    }

    public User getManager() {
        return manager;
    }

    public Car getCar() {
        return car;
    }

    public LocalDateTime getOrderedAt() {
        return orderedAt;
    }

    public InStockOrderStatus getStatus() {
        return status;
    }

    public void setStatus(InStockOrderStatus status) {
        this.status = requireNonNull(status, "order status");
    }
}
