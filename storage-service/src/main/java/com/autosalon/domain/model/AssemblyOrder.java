package com.autosalon.domain.model;

import com.autosalon.messaging.OrderKind;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "assembly_orders")
public class AssemblyOrder extends BaseEntity {
    @Column(nullable = false)
    private UUID sourceOrderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderKind orderKind;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssemblyOrderStatus status;

    @Column
    private String rejectionReason;

    protected AssemblyOrder() {
    }

    private AssemblyOrder(UUID sourceOrderId, OrderKind orderKind, AssemblyOrderStatus status, String rejectionReason) {
        this.sourceOrderId = sourceOrderId;
        this.orderKind = orderKind;
        this.status = status;
        this.rejectionReason = rejectionReason;
    }

    public static AssemblyOrder approved(UUID sourceOrderId, OrderKind orderKind) {
        return new AssemblyOrder(sourceOrderId, orderKind, AssemblyOrderStatus.APPROVED, null);
    }

    public static AssemblyOrder rejected(UUID sourceOrderId, OrderKind orderKind, String reason) {
        return new AssemblyOrder(sourceOrderId, orderKind, AssemblyOrderStatus.REJECTED, reason);
    }

    public UUID getSourceOrderId() {
        return sourceOrderId;
    }

    public OrderKind getOrderKind() {
        return orderKind;
    }

    public AssemblyOrderStatus getStatus() {
        return status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }
}
