package com.autosalon.domain.model;

import com.autosalon.domain.Identifiable;
import com.autosalon.domain.enums.BodyType;
import com.autosalon.domain.enums.ComponentType;
import com.autosalon.domain.enums.DriveType;
import com.autosalon.domain.enums.FuelType;
import com.autosalon.domain.enums.TransmissionType;
import com.autosalon.domain.exception.DomainValidationException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.autosalon.domain.validation.DomainValidator.requireId;
import static com.autosalon.domain.validation.DomainValidator.requireNonNull;
import static com.autosalon.domain.validation.DomainValidator.requireNotEmpty;
import static com.autosalon.domain.validation.DomainValidator.requirePositive;
import static com.autosalon.domain.validation.DomainValidator.requireText;

public final class CarModel implements Identifiable {
    private final UUID id;
    private final String brand;
    private final String name;
    private final BodyType bodyType;
    private final FuelType fuelType;
    private final int enginePowerHp;
    private final BigDecimal engineVolumeLiters;
    private final TransmissionType transmissionType;
    private final DriveType driveType;
    private final BigDecimal basePrice;
    private final Set<ComponentType> requiredComponentTypes;
    private final Map<ComponentType, UUID> baseComponentOptionIds = new EnumMap<>(ComponentType.class);

    public CarModel(
            UUID id,
            String brand,
            String name,
            BodyType bodyType,
            FuelType fuelType,
            int enginePowerHp,
            BigDecimal engineVolumeLiters,
            TransmissionType transmissionType,
            DriveType driveType,
            BigDecimal basePrice,
            Set<ComponentType> requiredComponentTypes
    ) {
        this.id = requireId(id, "model id");
        this.brand = requireText(brand, "brand");
        this.name = requireText(name, "model name");
        this.bodyType = requireNonNull(bodyType, "body type");
        this.fuelType = requireNonNull(fuelType, "fuel type");
        this.enginePowerHp = requirePositive(enginePowerHp, "engine power");
        this.engineVolumeLiters = requirePositive(engineVolumeLiters, "engine volume");
        this.transmissionType = requireNonNull(transmissionType, "transmission type");
        this.driveType = requireNonNull(driveType, "drive type");
        this.basePrice = requirePositive(basePrice, "base price");
        this.requiredComponentTypes = Collections.unmodifiableSet(
                EnumSet.copyOf(requireNotEmpty(requiredComponentTypes, "required component types"))
        );
    }

    public static CarModel create(
            String brand,
            String name,
            BodyType bodyType,
            FuelType fuelType,
            int enginePowerHp,
            BigDecimal engineVolumeLiters,
            TransmissionType transmissionType,
            DriveType driveType,
            BigDecimal basePrice,
            Set<ComponentType> requiredComponentTypes
    ) {
        return new CarModel(
                UUID.randomUUID(),
                brand,
                name,
                bodyType,
                fuelType,
                enginePowerHp,
                engineVolumeLiters,
                transmissionType,
                driveType,
                basePrice,
                requiredComponentTypes
        );
    }

    @Override
    public UUID getId() {
        return id;
    }

    public String getBrand() {
        return brand;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return brand + " " + name;
    }

    public BodyType getBodyType() {
        return bodyType;
    }

    public FuelType getFuelType() {
        return fuelType;
    }

    public int getEnginePowerHp() {
        return enginePowerHp;
    }

    public BigDecimal getEngineVolumeLiters() {
        return engineVolumeLiters;
    }

    public TransmissionType getTransmissionType() {
        return transmissionType;
    }

    public DriveType getDriveType() {
        return driveType;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public Set<ComponentType> getRequiredComponentTypes() {
        return requiredComponentTypes;
    }

    public Map<ComponentType, UUID> getBaseComponentOptionIds() {
        return Collections.unmodifiableMap(baseComponentOptionIds);
    }

    public void assignBaseComponent(ComponentType type, UUID componentOptionId) {
        requireNonNull(type, "component type");
        requireId(componentOptionId, "base component option id");
        if (!requiredComponentTypes.contains(type)) {
            throw new DomainValidationException(type + " is not required for " + getDisplayName());
        }
        baseComponentOptionIds.put(type, componentOptionId);
    }
}
