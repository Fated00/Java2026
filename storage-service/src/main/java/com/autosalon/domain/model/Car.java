package com.autosalon.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

import static com.autosalon.domain.validation.DomainValidator.requireId;
import static com.autosalon.domain.validation.DomainValidator.requireNonNull;
import static com.autosalon.domain.validation.DomainValidator.requirePositive;
import static com.autosalon.domain.validation.DomainValidator.requireText;

@Entity
@Table(name = "cars")
public class Car extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "model_id", nullable = false)
    private CarModel model;

    @Column(nullable = false)
    private String color;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private boolean available;

    @Column(nullable = false)
    private boolean testDriveAvailable;

    protected Car() {
    }

    public Car(UUID id, CarModel model, String color, BigDecimal price, boolean available) {
        super(requireId(id, "car id"));
        this.model = requireNonNull(model, "car model");
        this.color = requireText(color, "car color");
        this.price = requirePositive(price, "car price");
        this.available = available;
    }

    public static Car create(CarModel model, String color, BigDecimal price) {
        return new Car(UUID.randomUUID(), model, color, price, true);
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
