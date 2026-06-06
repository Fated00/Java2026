package com.autosalon.domain.model;

import com.autosalon.domain.Identifiable;
import com.autosalon.domain.enums.Role;
import com.autosalon.domain.exception.DomainValidationException;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.autosalon.domain.validation.DomainValidator.requireId;
import static com.autosalon.domain.validation.DomainValidator.requireNonNull;

public final class TestDriveRequest implements Identifiable {
    private final UUID id;
    private final User client;
    private final Car car;
    private final LocalDateTime startsAt;

    public TestDriveRequest(UUID id, User client, Car car, LocalDateTime startsAt) {
        this.id = requireId(id, "test-drive request id");
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

    @Override
    public UUID getId() {
        return id;
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
