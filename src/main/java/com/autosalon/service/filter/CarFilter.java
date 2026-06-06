package com.autosalon.service.filter;

import com.autosalon.domain.enums.BodyType;
import com.autosalon.domain.enums.DriveType;
import com.autosalon.domain.enums.FuelType;
import com.autosalon.domain.enums.TransmissionType;

import java.math.BigDecimal;

public final class CarFilter {
    private final BigDecimal minPrice;
    private final BigDecimal maxPrice;
    private final String brand;
    private final String model;
    private final BodyType bodyType;
    private final FuelType fuelType;
    private final Integer minPowerHp;
    private final Integer maxPowerHp;
    private final BigDecimal minEngineVolume;
    private final BigDecimal maxEngineVolume;
    private final TransmissionType transmissionType;
    private final DriveType driveType;
    private final String color;

    private CarFilter(Builder builder) {
        this.minPrice = builder.minPrice;
        this.maxPrice = builder.maxPrice;
        this.brand = builder.brand;
        this.model = builder.model;
        this.bodyType = builder.bodyType;
        this.fuelType = builder.fuelType;
        this.minPowerHp = builder.minPowerHp;
        this.maxPowerHp = builder.maxPowerHp;
        this.minEngineVolume = builder.minEngineVolume;
        this.maxEngineVolume = builder.maxEngineVolume;
        this.transmissionType = builder.transmissionType;
        this.driveType = builder.driveType;
        this.color = builder.color;
    }

    public static Builder builder() {
        return new Builder();
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public BodyType getBodyType() {
        return bodyType;
    }

    public FuelType getFuelType() {
        return fuelType;
    }

    public Integer getMinPowerHp() {
        return minPowerHp;
    }

    public Integer getMaxPowerHp() {
        return maxPowerHp;
    }

    public BigDecimal getMinEngineVolume() {
        return minEngineVolume;
    }

    public BigDecimal getMaxEngineVolume() {
        return maxEngineVolume;
    }

    public TransmissionType getTransmissionType() {
        return transmissionType;
    }

    public DriveType getDriveType() {
        return driveType;
    }

    public String getColor() {
        return color;
    }

    public boolean hasModelFilterWithoutBrand() {
        return model != null && !model.isBlank() && (brand == null || brand.isBlank());
    }

    public static final class Builder {
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private String brand;
        private String model;
        private BodyType bodyType;
        private FuelType fuelType;
        private Integer minPowerHp;
        private Integer maxPowerHp;
        private BigDecimal minEngineVolume;
        private BigDecimal maxEngineVolume;
        private TransmissionType transmissionType;
        private DriveType driveType;
        private String color;

        private Builder() {
        }

        public Builder priceFrom(BigDecimal minPrice) {
            this.minPrice = minPrice;
            return this;
        }

        public Builder priceTo(BigDecimal maxPrice) {
            this.maxPrice = maxPrice;
            return this;
        }

        public Builder brand(String brand) {
            this.brand = brand;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder bodyType(BodyType bodyType) {
            this.bodyType = bodyType;
            return this;
        }

        public Builder fuelType(FuelType fuelType) {
            this.fuelType = fuelType;
            return this;
        }

        public Builder powerFrom(int minPowerHp) {
            this.minPowerHp = minPowerHp;
            return this;
        }

        public Builder powerTo(int maxPowerHp) {
            this.maxPowerHp = maxPowerHp;
            return this;
        }

        public Builder engineVolumeFrom(BigDecimal minEngineVolume) {
            this.minEngineVolume = minEngineVolume;
            return this;
        }

        public Builder engineVolumeTo(BigDecimal maxEngineVolume) {
            this.maxEngineVolume = maxEngineVolume;
            return this;
        }

        public Builder transmissionType(TransmissionType transmissionType) {
            this.transmissionType = transmissionType;
            return this;
        }

        public Builder driveType(DriveType driveType) {
            this.driveType = driveType;
            return this;
        }

        public Builder color(String color) {
            this.color = color;
            return this;
        }

        public CarFilter build() {
            return new CarFilter(this);
        }
    }
}
