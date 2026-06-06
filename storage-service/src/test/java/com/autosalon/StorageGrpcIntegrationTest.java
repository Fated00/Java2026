package com.autosalon;

import com.autosalon.grpc.GrpcServerLifecycle;
import com.autosalon.grpc.inventory.CarInventoryServiceGrpc;
import com.autosalon.grpc.inventory.GetAvailableCarRequest;
import com.autosalon.grpc.inventory.ListAvailableCarsRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class StorageGrpcIntegrationTest extends StorageIntegrationTestBase {
    @Autowired
    private GrpcServerLifecycle grpcServerLifecycle;

    @Test
    void returnsAvailableCarsOverGrpc() {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", grpcServerLifecycle.getPort())
                .usePlaintext()
                .build();
        try {
            CarInventoryServiceGrpc.CarInventoryServiceBlockingStub stub =
                    CarInventoryServiceGrpc.newBlockingStub(channel);

            var cars = stub.listAvailableCars(ListAvailableCarsRequest.newBuilder().build()).getCarsList();
            var car = stub.getAvailableCar(GetAvailableCarRequest.newBuilder()
                    .setId("40000000-0000-0000-0000-000000000001")
                    .build());

            assertThat(cars).hasSize(2);
            assertThat(car.getFound()).isTrue();
            assertThat(car.getCar().getModelName()).isEqualTo("BMW 320i");
        } finally {
            channel.shutdownNow();
        }
    }
}
