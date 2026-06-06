package com.autosalon.service;

import com.autosalon.TestContext;
import com.autosalon.domain.enums.Role;
import com.autosalon.domain.exception.DomainValidationException;
import com.autosalon.domain.model.Car;
import com.autosalon.domain.model.CarModel;
import com.autosalon.domain.model.TestDriveRequest;
import com.autosalon.domain.model.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestDriveServiceTest {
    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-06-06T10:00:00Z"), ZoneOffset.UTC);

    @Test
    void createsFutureTestDriveRequestForAvailableCar() {
        TestContext context = new TestContext();
        TestDriveService service = new TestDriveService(
                context.userRepository,
                context.carRepository,
                context.testDriveRepository,
                FIXED_CLOCK
        );
        User client = context.userService.createUser("Client", Role.CLIENT);
        CarModel model = context.createBmw320();
        Car car = context.catalogService.addCar(model.getId(), "Black", BigDecimal.valueOf(4_050_000));

        service.addCarToTestDriveList(car.getId());
        LocalDateTime start = LocalDateTime.of(2026, 6, 7, 12, 0);
        TestDriveRequest request = service.requestTestDrive(client.getId(), car.getId(), start);

        assertEquals(client.getId(), request.getClient().getId());
        assertEquals(car.getId(), request.getCar().getId());
        assertEquals(start, service.findRequest(request.getId()).getStartsAt());
        assertEquals(1, service.listRequests().size());
    }

    @Test
    void rejectsUnavailableCarAndAllowsRemovingFromList() {
        TestContext context = new TestContext();
        TestDriveService service = new TestDriveService(
                context.userRepository,
                context.carRepository,
                context.testDriveRepository,
                FIXED_CLOCK
        );
        User client = context.userService.createUser("Client", Role.CLIENT);
        CarModel model = context.createBmw320();
        Car car = context.catalogService.addCar(model.getId(), "Black", BigDecimal.valueOf(4_050_000));

        assertThrows(
                DomainValidationException.class,
                () -> service.requestTestDrive(client.getId(), car.getId(), LocalDateTime.of(2026, 6, 7, 12, 0))
        );

        service.addCarToTestDriveList(car.getId());
        assertTrue(context.catalogService.findCar(car.getId()).isTestDriveAvailable());
        service.removeCarFromTestDriveList(car.getId());
        assertFalse(context.catalogService.findCar(car.getId()).isTestDriveAvailable());
    }

    @Test
    void rejectsPastStartTimeAndDuplicateSlot() {
        TestContext context = new TestContext();
        TestDriveService service = new TestDriveService(
                context.userRepository,
                context.carRepository,
                context.testDriveRepository,
                FIXED_CLOCK
        );
        User client = context.userService.createUser("Client", Role.CLIENT);
        CarModel model = context.createBmw320();
        Car car = context.catalogService.addCar(model.getId(), "Black", BigDecimal.valueOf(4_050_000));
        service.addCarToTestDriveList(car.getId());

        assertThrows(
                DomainValidationException.class,
                () -> service.requestTestDrive(client.getId(), car.getId(), LocalDateTime.of(2026, 6, 5, 12, 0))
        );

        LocalDateTime future = LocalDateTime.of(2026, 6, 7, 12, 0);
        service.requestTestDrive(client.getId(), car.getId(), future);

        assertThrows(
                DomainValidationException.class,
                () -> service.requestTestDrive(client.getId(), car.getId(), future)
        );
    }
}
