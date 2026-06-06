package com.autosalon.domain.model;

import com.autosalon.domain.enums.BodyType;
import com.autosalon.domain.enums.ComponentType;
import com.autosalon.domain.enums.DriveType;
import com.autosalon.domain.enums.FuelType;
import com.autosalon.domain.enums.TransmissionType;
import com.autosalon.domain.exception.DomainValidationException;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.MapKeyEnumerated;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.autosalon.domain.validation.DomainValidator.requireId;
import static com.autosalon.domain.validation.DomainValidator.requireNonNull;
import static com.autosalon.domain.validation.DomainValidator.requireNotEmpty;
import static com.autosalon.domain.validation.DomainValidator.requirePositive;
import static com.autosalon.domain.validation.DomainValidator.requireText;

@Entity
@Table(name = "car_models")
public class CarModel extends BaseEntity {
    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BodyType bodyType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FuelType fuelType;

    @Column(nullable = false)
    private int enginePowerHp;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal engineVolumeLiters;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransmissionType transmissionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DriveType driveType;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal basePrice;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "car_model_required_components", joinColumns = @JoinColumn(name = "model_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "component_type", nullable = false)
    private Set<ComponentType> requiredComponentTypes = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "car_model_base_component_options",
            joinColumns = @JoinColumn(name = "model_id"),
            inverseJoinColumns = @JoinColumn(name = "component_option_id")
    )
    @MapKeyColumn(name = "component_type", nullable = false)
    @MapKeyEnumerated(EnumType.STRING)
    private Map<ComponentType, ComponentOption> baseComponentOptions = new EnumMap<>(ComponentType.class);

    protected CarModel() {
    }

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
        super(requireId(id, "model id"));
        this.brand = requireText(brand, "brand");
        this.name = requireText(name, "model name");
        this.bodyType = requireNonNull(bodyType, "body type");
        this.fuelType = requireNonNull(fuelType, "fuel type");
        this.enginePowerHp = requirePositive(enginePowerHp, "engine power");
        this.engineVolumeLiters = requirePositive(engineVolumeLiters, "engine volume");
        this.transmissionType = requireNonNull(transmissionType, "transmission type");
        this.driveType = requireNonNull(driveType, "drive type");
        this.basePrice = requirePositive(basePrice, "base price");
        this.requiredComponentTypes = EnumSet.copyOf(requireNotEmpty(requiredComponentTypes, "required component types"));
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
        return Collections.unmodifiableSet(requiredComponentTypes);
    }

    public Map<ComponentType, ComponentOption> getBaseComponentOptions() {
        return Collections.unmodifiableMap(baseComponentOptions);
    }

    public Map<ComponentType, UUID> getBaseComponentOptionIds() {
        Map<ComponentType, UUID> ids = new EnumMap<>(ComponentType.class);
        baseComponentOptions.forEach((type, option) -> ids.put(type, option.getId()));
        return Collections.unmodifiableMap(ids);
    }

    public void assignBaseComponent(ComponentType type, ComponentOption componentOption) {
        requireNonNull(type, "component type");
        requireNonNull(componentOption, "base component option");
        if (!requiredComponentTypes.contains(type)) {
            throw new DomainValidationException(type + " is not required for " + getDisplayName());
        }
        baseComponentOptions.put(type, componentOption);
    }
}
