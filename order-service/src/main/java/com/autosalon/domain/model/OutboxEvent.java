package com.autosalon.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

import static com.autosalon.domain.validation.DomainValidator.requireId;
import static com.autosalon.domain.validation.DomainValidator.requireNonNull;
import static com.autosalon.domain.validation.DomainValidator.requireText;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent extends BaseEntity {
    @Column(nullable = false)
    private UUID aggregateId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxEventStatus status;

    @Column
    private Instant publishedAt;

    protected OutboxEvent() {
    }

    public OutboxEvent(UUID id, UUID aggregateId, String eventType, String payload) {
        super(requireId(id, "outbox event id"));
        this.aggregateId = requireNonNull(aggregateId, "aggregate id");
        this.eventType = requireText(eventType, "event type");
        this.payload = requireText(payload, "payload");
        this.status = OutboxEventStatus.NEW;
    }

    public static OutboxEvent create(UUID aggregateId, String eventType, String payload) {
        return new OutboxEvent(UUID.randomUUID(), aggregateId, eventType, payload);
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public OutboxEventStatus getStatus() {
        return status;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void markPublished() {
        this.status = OutboxEventStatus.PUBLISHED;
        this.publishedAt = Instant.now();
    }
}
