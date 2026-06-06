package com.autosalon.service;

import com.autosalon.grpc.inventory.AvailableCarsResponse;
import com.autosalon.grpc.inventory.CarInventoryServiceGrpc;
import com.autosalon.grpc.inventory.ListAvailableCarsRequest;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StorageGrpcClientTest {
    @Test
    void returnsEmptyListWhenStorageHasNoAvailableCars() throws Exception {
        Server server = ServerBuilder.forPort(0)
                .addService(new EmptyInventoryService())
                .build()
                .start();
        StorageGrpcClient client = client(server.getPort(), 500);
        try {
            assertThat(client.listAvailableCars()).isEmpty();
        } finally {
            client.shutdown();
            server.shutdownNow();
        }
    }

    @Test
    void mapsTimeoutToServiceUnavailable() throws Exception {
        Server server = ServerBuilder.forPort(0)
                .addService(new SlowInventoryService())
                .build()
                .start();
        StorageGrpcClient client = client(server.getPort(), 50);
        try {
            assertThatThrownBy(client::listAvailableCars)
                    .isInstanceOf(StorageServiceUnavailableException.class);
        } finally {
            client.shutdown();
            server.shutdownNow();
        }
    }

    @Test
    void mapsUnavailableStorageToServiceUnavailable() throws Exception {
        int port = unusedPort();
        StorageGrpcClient client = client(port, 100);
        try {
            assertThatThrownBy(client::listAvailableCars)
                    .isInstanceOf(StorageServiceUnavailableException.class);
        } finally {
            client.shutdown();
        }
    }

    private StorageGrpcClient client(int port, long timeoutMs) {
        return new StorageGrpcClient(
                ManagedChannelBuilder.forAddress("localhost", port).usePlaintext().build(),
                timeoutMs
        );
    }

    private int unusedPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private static class EmptyInventoryService extends CarInventoryServiceGrpc.CarInventoryServiceImplBase {
        @Override
        public void listAvailableCars(
                ListAvailableCarsRequest request,
                StreamObserver<AvailableCarsResponse> responseObserver
        ) {
            responseObserver.onNext(AvailableCarsResponse.newBuilder().build());
            responseObserver.onCompleted();
        }
    }

    private static class SlowInventoryService extends CarInventoryServiceGrpc.CarInventoryServiceImplBase {
        @Override
        public void listAvailableCars(
                ListAvailableCarsRequest request,
                StreamObserver<AvailableCarsResponse> responseObserver
        ) {
            try {
                TimeUnit.MILLISECONDS.sleep(300);
                responseObserver.onNext(AvailableCarsResponse.newBuilder().build());
                responseObserver.onCompleted();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                responseObserver.onError(exception);
            }
        }
    }
}
