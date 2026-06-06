package com.autosalon.service;

import com.autosalon.api.dto.Dtos.CarDto;
import com.autosalon.domain.exception.EntityNotFoundException;
import com.autosalon.grpc.inventory.AvailableCar;
import com.autosalon.grpc.inventory.CarInventoryServiceGrpc;
import com.autosalon.grpc.inventory.GetAvailableCarRequest;
import com.autosalon.grpc.inventory.ListAvailableCarsRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class StorageGrpcClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageGrpcClient.class);

    private final ManagedChannel channel;
    private final CarInventoryServiceGrpc.CarInventoryServiceBlockingStub blockingStub;
    private final long timeoutMs;

    @Autowired
    public StorageGrpcClient(
            @Value("${autosalon.grpc.storage.host:localhost}") String host,
            @Value("${autosalon.grpc.storage.port:9090}") int port,
            @Value("${autosalon.grpc.storage.timeout-ms:1000}") long timeoutMs
    ) {
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext().build(), timeoutMs);
        LOGGER.info("Storage gRPC client configured for {}:{} with timeout {} ms", host, port, timeoutMs);
    }

    StorageGrpcClient(ManagedChannel channel, long timeoutMs) {
        this.channel = channel;
        this.blockingStub = CarInventoryServiceGrpc.newBlockingStub(channel);
        this.timeoutMs = timeoutMs;
    }

    public List<CarDto> listAvailableCars() {
        try {
            LOGGER.info("Calling StorageService gRPC listAvailableCars");
            return blockingStub.withDeadlineAfter(timeoutMs, TimeUnit.MILLISECONDS)
                    .listAvailableCars(ListAvailableCarsRequest.newBuilder().build())
                    .getCarsList()
                    .stream()
                    .map(this::toDto)
                    .toList();
        } catch (StatusRuntimeException exception) {
            throw mapGrpcException(exception);
        }
    }

    public CarDto getAvailableCar(UUID id) {
        try {
            LOGGER.info("Calling StorageService gRPC getAvailableCar for {}", id);
            var response = blockingStub.withDeadlineAfter(timeoutMs, TimeUnit.MILLISECONDS)
                    .getAvailableCar(GetAvailableCarRequest.newBuilder().setId(id.toString()).build());
            if (!response.getFound()) {
                throw new EntityNotFoundException("Car", id);
            }
            return toDto(response.getCar());
        } catch (StatusRuntimeException exception) {
            throw mapGrpcException(exception);
        }
    }

    @PreDestroy
    public void shutdown() {
        channel.shutdown();
    }

    private RuntimeException mapGrpcException(StatusRuntimeException exception) {
        Status.Code code = exception.getStatus().getCode();
        if (code == Status.Code.UNAVAILABLE || code == Status.Code.DEADLINE_EXCEEDED) {
            return new StorageServiceUnavailableException("StorageService is unavailable", exception);
        }
        return new StorageServiceUnavailableException("StorageService gRPC call failed", exception);
    }

    private CarDto toDto(AvailableCar car) {
        return new CarDto(
                UUID.fromString(car.getId()),
                UUID.fromString(car.getModelId()),
                car.getModelName(),
                car.getColor(),
                new BigDecimal(car.getPrice()),
                car.getAvailable(),
                car.getTestDriveAvailable()
        );
    }
}
