package com.autosalon.service;

import com.autosalon.domain.enums.Role;
import com.autosalon.domain.exception.DomainValidationException;
import com.autosalon.domain.model.Car;
import com.autosalon.domain.model.TestDriveRequest;
import com.autosalon.domain.model.User;
import com.autosalon.repository.CrudRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.autosalon.domain.validation.DomainValidator.requireNonNull;

public final class TestDriveService {
    private final CrudRepository<User> userRepository;
    private final CrudRepository<Car> carRepository;
    private final CrudRepository<TestDriveRequest> requestRepository;
    private final Clock clock;

    public TestDriveService(
            CrudRepository<User> userRepository,
            CrudRepository<Car> carRepository,
            CrudRepository<TestDriveRequest> requestRepository
    ) {
        this(userRepository, carRepository, requestRepository, Clock.systemDefaultZone());
    }

    public TestDriveService(
            CrudRepository<User> userRepository,
            CrudRepository<Car> carRepository,
            CrudRepository<TestDriveRequest> requestRepository,
            Clock clock
    ) {
        this.userRepository = requireNonNull(userRepository, "user repository");
        this.carRepository = requireNonNull(carRepository, "car repository");
        this.requestRepository = requireNonNull(requestRepository, "test-drive request repository");
        this.clock = requireNonNull(clock, "clock");
    }

    public Car addCarToTestDriveList(UUID carId) {
        Car car = ServiceSupport.findOrThrow(carRepository, carId, "Car");
        car.setTestDriveAvailable(true);
        return carRepository.save(car);
    }

    public Car removeCarFromTestDriveList(UUID carId) {
        Car car = ServiceSupport.findOrThrow(carRepository, carId, "Car");
        car.setTestDriveAvailable(false);
        return carRepository.save(car);
    }

    public TestDriveRequest requestTestDrive(UUID clientId, UUID carId, LocalDateTime startsAt) {
        User client = ServiceSupport.requireRole(
                ServiceSupport.findOrThrow(userRepository, clientId, "User"),
                Role.CLIENT,
                "client"
        );
        Car car = ServiceSupport.findOrThrow(carRepository, carId, "Car");
        LocalDateTime startTime = requireNonNull(startsAt, "test-drive start time");

        if (!car.isTestDriveAvailable()) {
            throw new DomainValidationException("car is not available for test-drive");
        }
        if (!startTime.isAfter(LocalDateTime.now(clock))) {
            throw new DomainValidationException("test-drive start time must be in the future");
        }
        if (isSlotAlreadyBooked(carId, startTime)) {
            throw new DomainValidationException("test-drive slot is already booked for this car");
        }

        return requestRepository.save(TestDriveRequest.create(client, car, startTime));
    }

    public TestDriveRequest findRequest(UUID id) {
        return ServiceSupport.findOrThrow(requestRepository, id, "TestDriveRequest");
    }

    public List<TestDriveRequest> listRequests() {
        return requestRepository.findAll();
    }

    private boolean isSlotAlreadyBooked(UUID carId, LocalDateTime startsAt) {
        return requestRepository.findAll().stream()
                .anyMatch(request -> request.getCar().getId().equals(carId) && request.getStartsAt().equals(startsAt));
    }
}
