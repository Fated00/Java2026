package com.autosalon.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class GrpcServerLifecycle implements SmartLifecycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcServerLifecycle.class);

    private final CarInventoryGrpcService carInventoryGrpcService;
    private final int configuredPort;
    private Server server;
    private boolean running;

    public GrpcServerLifecycle(
            CarInventoryGrpcService carInventoryGrpcService,
            @Value("${autosalon.grpc.server.port:9090}") int configuredPort
    ) {
        this.carInventoryGrpcService = carInventoryGrpcService;
        this.configuredPort = configuredPort;
    }

    @Override
    public void start() {
        try {
            server = ServerBuilder.forPort(configuredPort)
                    .addService(carInventoryGrpcService)
                    .build()
                    .start();
            running = true;
            LOGGER.info("Storage gRPC server started on port {}", getPort());
        } catch (IOException exception) {
            throw new IllegalStateException("failed to start storage gRPC server", exception);
        }
    }

    @Override
    public void stop() {
        if (server == null) {
            running = false;
            return;
        }
        server.shutdown();
        try {
            if (!server.awaitTermination(5, TimeUnit.SECONDS)) {
                server.shutdownNow();
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            server.shutdownNow();
        } finally {
            running = false;
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    public int getPort() {
        return server == null ? configuredPort : server.getPort();
    }
}
