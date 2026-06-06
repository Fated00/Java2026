package com.autosalon.domain.model;

import com.autosalon.domain.Identifiable;

import java.math.BigDecimal;
import java.util.UUID;

import static com.autosalon.domain.validation.DomainValidator.requireId;
import static com.autosalon.domain.validation.DomainValidator.requireNonNull;
import static com.autosalon.domain.validation.DomainValidator.requirePositive;
import static com.autosalon.domain.validation.DomainValidator.requireText;

public final class Car implements Identifiable {
    private final UUID id;
    private final CarModel model;
    private String color;
    private BigDecimal price;
    private boolean available;
    private boolean testDriveAvailable;

    public Car(UUID id, CarModel model, String color, BigDecimal price, boolean available) {
        this.id = requireId(id, "car id");
        this.model = requireNonNull(model, "car model");
        this.color = requireText(color, "car color");
        this.price = requirePositive(price, "car price");
        this.available = available;
    }

    public static Car create(CarModel model, String color, BigDecimal price) {
        return new Car(UUID.randomUUID(), model, color, price, true);
    }

    @Override
    public UUID getId() {
        return id;
    }

    public CarModel getModel() {
        return model;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = requireText(color, "car color");
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = requirePositive(price, "car price");
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public boolean isTestDriveAvailable() {
        return testDriveAvailable;
    }

    public void setTestDriveAvailable(boolean testDriveAvailable) {
        this.testDriveAvailable = testDriveAvailable;
    }
}
