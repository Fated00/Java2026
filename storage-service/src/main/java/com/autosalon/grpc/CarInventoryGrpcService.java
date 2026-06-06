package com.autosalon.grpc;

import com.autosalon.domain.exception.EntityNotFoundException;
import com.autosalon.domain.model.Car;
import com.autosalon.grpc.inventory.AvailableCar;
import com.autosalon.grpc.inventory.AvailableCarResponse;
import com.autosalon.grpc.inventory.AvailableCarsResponse;
import com.autosalon.grpc.inventory.CarInventoryServiceGrpc;
import com.autosalon.grpc.inventory.GetAvailableCarRequest;
import com.autosalon.grpc.inventory.ListAvailableCarsRequest;
import com.autosalon.service.CarCatalogService;
import com.autosalon.service.filter.CarFilter;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CarInventoryGrpcService extends CarInventoryServiceGrpc.CarInventoryServiceImplBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(CarInventoryGrpcService.class);

    private final CarCatalogService carCatalogService;

    public CarInventoryGrpcService(CarCatalogService carCatalogService) {
        this.carCatalogService = carCatalogService;
    }

    @Override
    public void listAvailableCars(
            ListAvailableCarsRequest request,
            StreamObserver<AvailableCarsResponse> responseObserver
    ) {
        LOGGER.info("gRPC listAvailableCars request received");
        AvailableCarsResponse.Builder response = AvailableCarsResponse.newBuilder();
        carCatalogService.listAvailableCars(CarFilter.builder().build()).stream()
                .map(this::toGrpcCar)
                .forEach(response::addCars);
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getAvailableCar(
            GetAvailableCarRequest request,
            StreamObserver<AvailableCarResponse> responseObserver
    ) {
        LOGGER.info("gRPC getAvailableCar request received for {}", request.getId());
        try {
            Car car = carCatalogService.findCar(java.util.UUID.fromString(request.getId()));
            AvailableCarResponse response = AvailableCarResponse.newBuilder()
                    .setFound(car.isAvailable())
                    .setCar(car.isAvailable() ? toGrpcCar(car) : AvailableCar.getDefaultInstance())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException | EntityNotFoundException exception) {
            responseObserver.onNext(AvailableCarResponse.newBuilder().setFound(false).build());
            responseObserver.onCompleted();
        }
    }

    private AvailableCar toGrpcCar(Car car) {
        return AvailableCar.newBuilder()
                .setId(car.getId().toString())
                .setModelId(car.getModel().getId().toString())
                .setModelName(car.getModel().getDisplayName())
                .setColor(car.getColor())
                .setPrice(car.getPrice().toPlainString())
                .setAvailable(car.isAvailable())
                .setTestDriveAvailable(car.isTestDriveAvailable())
                .build();
    }
}
