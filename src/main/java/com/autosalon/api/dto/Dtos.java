package com.autosalon.api.dto;

import com.autosalon.domain.enums.BodyType;
import com.autosalon.domain.enums.ComponentType;
import com.autosalon.domain.enums.CustomOrderStatus;
import com.autosalon.domain.enums.DriveType;
import com.autosalon.domain.enums.FuelType;
import com.autosalon.domain.enums.InStockOrderStatus;
import com.autosalon.domain.enums.Role;
import com.autosalon.domain.enums.TransmissionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class Dtos {
    private Dtos() {
    }

    public record ApiError(String code, String message, Instant timestamp) {
    }

    public record UserDto(UUID id, String fullName, Role role) {
    }

    public record CreateUserRequest(@NotBlank String fullName, @NotNull Role role) {
    }

    public record CarModelDto(
            UUID id,
            String brand,
            String name,
            String displayName,
            BodyType bodyType,
            FuelType fuelType,
            int enginePowerHp,
            BigDecimal engineVolumeLiters,
            TransmissionType transmissionType,
            DriveType driveType,
            BigDecimal basePrice,
            Set<ComponentType> requiredComponentTypes,
            Map<ComponentType, UUID> baseComponentOptionIds
    ) {
    }

    public record CreateCarModelRequest(
            @NotBlank String brand,
            @NotBlank String name,
            @NotNull BodyType bodyType,
            @NotNull FuelType fuelType,
            @Positive int enginePowerHp,
            @NotNull @DecimalMin("0.1") BigDecimal engineVolumeLiters,
            @NotNull TransmissionType transmissionType,
            @NotNull DriveType driveType,
            @NotNull @DecimalMin("0.01") BigDecimal basePrice,
            @NotEmpty Set<ComponentType> requiredComponentTypes
    ) {
    }

    public record CarDto(
            UUID id,
            UUID modelId,
            String modelName,
            String color,
            BigDecimal price,
            boolean available,
            boolean testDriveAvailable
    ) {
    }

    public record CreateCarRequest(@NotNull UUID modelId, @NotBlank String color, @NotNull BigDecimal price) {
    }

    public record UpdateCarRequest(String color, BigDecimal price, Boolean available) {
    }

    public record PartDto(
            UUID id,
            String sku,
            String name,
            BigDecimal price,
            Set<UUID> compatibleModelIds
    ) {
    }

    public record CreatePartRequest(
            @NotBlank String sku,
            @NotBlank String name,
            @NotNull BigDecimal price,
            @NotEmpty Set<UUID> compatibleModelIds
    ) {
    }

    public record ComponentOptionDto(
            UUID id,
            ComponentType type,
            String name,
            BigDecimal priceDelta,
            Set<UUID> compatibleModelIds
    ) {
    }

    public record RegisterComponentOptionRequest(
            @NotNull ComponentType type,
            @NotBlank String name,
            @NotNull BigDecimal priceDelta,
            @NotEmpty Set<UUID> compatibleModelIds
    ) {
    }

    public record AssignBaseComponentRequest(
            @NotNull UUID modelId,
            @NotNull ComponentType type,
            @NotNull UUID componentOptionId
    ) {
    }

    public record ConfigurationDto(
            UUID modelId,
            String modelName,
            Map<ComponentType, ComponentOptionDto> selectedComponents,
            BigDecimal totalPrice
    ) {
    }

    public record BuildConfigurationRequest(
            @NotNull UUID modelId,
            @NotEmpty Map<ComponentType, UUID> selectedOptionIds
    ) {
    }

    public record CreateInStockOrderRequest(@NotNull UUID clientId, @NotNull UUID carId) {
    }

    public record InStockOrderDto(
            UUID id,
            UUID clientId,
            UUID managerId,
            UUID carId,
            InStockOrderStatus status,
            LocalDateTime orderedAt
    ) {
    }

    public record CreateCustomOrderRequest(
            @NotNull UUID clientId,
            @NotNull UUID modelId,
            @NotEmpty Map<ComponentType, UUID> selectedOptionIds
    ) {
    }

    public record CustomOrderDto(
            UUID id,
            UUID clientId,
            UUID managerId,
            ConfigurationDto configuration,
            CustomOrderStatus status,
            LocalDateTime orderedAt
    ) {
    }

    public record CreateTestDriveRequest(
            @NotNull UUID clientId,
            @NotNull UUID carId,
            @NotNull @Future LocalDateTime startsAt
    ) {
    }

    public record TestDriveRequestDto(UUID id, UUID clientId, UUID carId, LocalDateTime startsAt) {
    }
}
