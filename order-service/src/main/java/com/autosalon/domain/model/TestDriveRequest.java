package com.autosalon.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.autosalon.domain.validation.DomainValidator.requireId;
import static com.autosalon.domain.validation.DomainValidator.requireNonNull;

@Entity
@Table(name = "test_drive_requests")
public class TestDriveRequest extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @Column(nullable = false)
    private UUID carId;

    @Column(nullable = false)
    private LocalDateTime startsAt;

    protected TestDriveRequest() {
    }

    public TestDriveRequest(UUID id, User client, UUID carId, LocalDateTime startsAt) {
        super(requireId(id, "test-drive request id"));
        this.client = requireNonNull(client, "client");
        this.carId = requireNonNull(carId, "car id");
        this.startsAt = requireNonNull(startsAt, "starts at");
    }

    public static TestDriveRequest create(User client, UUID carId, LocalDateTime startsAt) {
        return new TestDriveRequest(UUID.randomUUID(), client, carId, startsAt);
    }

    public User getClient() {
        return client;
    }

    public UUID getCarId() {
        return carId;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }
}
