package com.autosalon.service;

import com.autosalon.TestContext;
import com.autosalon.domain.enums.BodyType;
import com.autosalon.domain.enums.ComponentType;
import com.autosalon.domain.enums.DriveType;
import com.autosalon.domain.enums.FuelType;
import com.autosalon.domain.enums.TransmissionType;
import com.autosalon.domain.exception.DomainValidationException;
import com.autosalon.domain.exception.EntityNotFoundException;
import com.autosalon.domain.model.Car;
import com.autosalon.domain.model.CarModel;
import com.autosalon.domain.model.Part;
import com.autosalon.service.filter.CarFilter;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CarCatalogServiceTest {
    @Test
    void filtersAvailableCarsByRequiredFields() {
        TestContext context = new TestContext();
        CarModel bmw320 = context.createBmw320();
        CarModel audiA4 = context.catalogService.createModel(
                "Audi",
                "A4",
                BodyType.SEDAN,
                FuelType.GASOLINE,
                190,
                BigDecimal.valueOf(2.0),
                TransmissionType.AUTOMATIC,
                DriveType.FRONT,
                BigDecimal.valueOf(3_700_000),
                EnumSet.allOf(ComponentType.class)
        );
        Car expected = context.catalogService.addCar(bmw320.getId(), "Black", BigDecimal.valueOf(4_050_000));
        Car hidden = context.catalogService.addCar(bmw320.getId(), "White", BigDecimal.valueOf(4_200_000));
        context.catalogService.addCar(audiA4.getId(), "Black", BigDecimal.valueOf(3_900_000));
        hidden.setAvailable(false);

        var result = context.catalogService.listAvailableCars(
                CarFilter.builder()
                        .brand("bmw")
                        .model("320i")
                        .bodyType(BodyType.SEDAN)
                        .fuelType(FuelType.GASOLINE)
                        .powerFrom(180)
                        .powerTo(200)
                        .engineVolumeFrom(BigDecimal.valueOf(1.9))
                        .engineVolumeTo(BigDecimal.valueOf(2.1))
                        .transmissionType(TransmissionType.AUTOMATIC)
                        .driveType(DriveType.REAR)
                        .color("black")
                        .priceFrom(BigDecimal.valueOf(4_000_000))
                        .priceTo(BigDecimal.valueOf(4_100_000))
                        .build()
        );

        assertEquals(1, result.size());
        assertEquals(expected.getId(), result.getFirst().getId());
    }

    @Test
    void rejectsModelFilterWithoutBrand() {
        TestContext context = new TestContext();

        assertThrows(
                DomainValidationException.class,
                () -> context.catalogService.listAvailableCars(CarFilter.builder().model("320i").build())
        );
    }

    @Test
    void createsAndUpdatesParts() {
        TestContext context = new TestContext();
        CarModel bmw320 = context.createBmw320();

        Part part = context.catalogService.addPart(
                "BMW-FILTER-001",
                "Oil filter",
                BigDecimal.valueOf(4_900),
                Set.of(bmw320.getId())
        );
        Part updated = context.catalogService.updatePart(
                part.getId(),
                "Premium oil filter",
                BigDecimal.valueOf(5_300),
                Set.of(bmw320.getId())
        );

        assertEquals("BMW-FILTER-001", updated.getSku());
        assertEquals("Premium oil filter", updated.getName());
        assertEquals(0, BigDecimal.valueOf(5_300).compareTo(updated.getPrice()));
        assertTrue(context.catalogService.listParts().contains(updated));
    }

    @Test
    void rejectsPartCompatibleWithUnknownModel() {
        TestContext context = new TestContext();

        assertThrows(
                EntityNotFoundException.class,
                () -> context.catalogService.addPart(
                        "UNKNOWN",
                        "Unknown part",
                        BigDecimal.TEN,
                        Set.of(UUID.randomUUID())
                )
        );
    }

    @Test
    void updatesAndDeletesCar() {
        TestContext context = new TestContext();
        CarModel bmw320 = context.createBmw320();
        Car car = context.catalogService.addCar(bmw320.getId(), "Black", BigDecimal.valueOf(4_050_000));

        context.catalogService.updateCar(car.getId(), "Blue", BigDecimal.valueOf(4_060_000), false);
        Car updated = context.catalogService.findCar(car.getId());

        assertEquals("Blue", updated.getColor());
        assertEquals(0, BigDecimal.valueOf(4_060_000).compareTo(updated.getPrice()));
        assertTrue(context.catalogService.listCars().contains(updated));

        context.catalogService.deleteCar(car.getId());
        assertThrows(EntityNotFoundException.class, () -> context.catalogService.findCar(car.getId()));
    }
}
