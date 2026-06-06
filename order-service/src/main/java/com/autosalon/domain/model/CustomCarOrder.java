package com.autosalon.domain.model;

import com.autosalon.domain.enums.ComponentType;
import com.autosalon.domain.enums.CustomOrderStatus;
import com.autosalon.domain.enums.Role;
import com.autosalon.domain.exception.DomainValidationException;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.MapKeyEnumerated;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

import static com.autosalon.domain.validation.DomainValidator.requireId;
import static com.autosalon.domain.validation.DomainValidator.requireNonNull;

@Entity
@Table(name = "custom_car_orders")
public class CustomCarOrder extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(optional = false)
    @JoinColumn(name = "manager_id", nullable = false)
    private User manager;

    @Column(nullable = false)
    private UUID modelId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "custom_order_components", joinColumns = @JoinColumn(name = "custom_order_id"))
    @MapKeyColumn(name = "component_type", nullable = false)
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name = "component_option_id", nullable = false)
    private Map<ComponentType, UUID> selectedOptionIds = new EnumMap<>(ComponentType.class);

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal totalPrice;

    @Column(nullable = false)
    private LocalDateTime orderedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomOrderStatus status;

    protected CustomCarOrder() {
    }

    public CustomCarOrder(UUID id, User client, User manager, UUID modelId, Map<ComponentType, UUID> selectedOptionIds) {
        super(requireId(id, "custom order id"));
        this.client = requireUserRole(client, Role.CLIENT, "client");
        this.manager = requireUserRole(manager, Role.DEALERSHIP_MANAGER, "manager");
        this.modelId = requireNonNull(modelId, "model id");
        Map<ComponentType, UUID> checkedOptions = requireNonNull(selectedOptionIds, "selected option ids");
        if (checkedOptions.isEmpty()) {
            throw new DomainValidationException("selected option ids must not be empty");
        }
        this.selectedOptionIds = new EnumMap<>(checkedOptions);
        this.totalPrice = BigDecimal.ZERO;
        this.orderedAt = LocalDateTime.now();
        this.status = CustomOrderStatus.CREATED;
    }

    public static CustomCarOrder create(User client, User manager, UUID modelId, Map<ComponentType, UUID> selectedOptionIds) {
        return new CustomCarOrder(UUID.randomUUID(), client, manager, modelId, selectedOptionIds);
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

    public UUID getModelId() {
        return modelId;
    }

    public Map<ComponentType, UUID> getSelectedOptionIds() {
        return Map.copyOf(selectedOptionIds);
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public LocalDateTime getOrderedAt() {
        return orderedAt;
    }

    public CustomOrderStatus getStatus() {
        return status;
    }

    public void setStatus(CustomOrderStatus status) {
        this.status = requireNonNull(status, "custom order status");
    }
}
