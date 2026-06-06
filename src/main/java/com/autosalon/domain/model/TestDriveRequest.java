package com.autosalon.domain.model;

import com.autosalon.domain.enums.Role;
import com.autosalon.domain.exception.DomainValidationException;
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

    @ManyToOne(optional = false)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @Column(nullable = false)
    private LocalDateTime startsAt;

    protected TestDriveRequest() {
    }

    public TestDriveRequest(UUID id, User client, Car car, LocalDateTime startsAt) {
        super(requireId(id, "test-drive request id"));
        this.client = requireClient(client);
        this.car = requireNonNull(car, "car");
        this.startsAt = requireNonNull(startsAt, "test-drive start time");
    }

    public static TestDriveRequest create(User client, Car car, LocalDateTime startsAt) {
        return new TestDriveRequest(UUID.randomUUID(), client, car, startsAt);
    }

    private static User requireClient(User user) {
        requireNonNull(user, "client");
        if (user.getRole() != Role.CLIENT) {
            throw new DomainValidationException("test-drive request client must have role CLIENT");
        }
        return user;
    }

    public User getClient() {
        return client;
    }

    public Car getCar() {
        return car;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }
}
