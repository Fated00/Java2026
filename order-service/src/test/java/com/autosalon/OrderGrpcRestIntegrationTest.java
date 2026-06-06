package com.autosalon;

import com.autosalon.grpc.inventory.AvailableCar;
import com.autosalon.grpc.inventory.AvailableCarResponse;
import com.autosalon.grpc.inventory.AvailableCarsResponse;
import com.autosalon.grpc.inventory.CarInventoryServiceGrpc;
import com.autosalon.grpc.inventory.GetAvailableCarRequest;
import com.autosalon.grpc.inventory.ListAvailableCarsRequest;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderGrpcRestIntegrationTest extends OrderIntegrationTestBase {
    private static final UUID EGOR_USER_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final Server GRPC_SERVER;
    private static final int GRPC_PORT;

    static {
        try {
            GRPC_SERVER = ServerBuilder.forPort(0)
                    .addService(new FakeCarInventoryService())
                    .build()
                    .start();
            GRPC_PORT = GRPC_SERVER.getPort();
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void registerGrpcProperties(DynamicPropertyRegistry registry) {
        registry.add("autosalon.grpc.storage.host", () -> "localhost");
        registry.add("autosalon.grpc.storage.port", () -> GRPC_PORT);
        registry.add("autosalon.grpc.storage.timeout-ms", () -> "500");
    }

    @Test
    void exposesAvailableCarsThroughRestBackedByGrpcClient() throws Exception {
        mockMvc.perform(get("/api/v1/cars")
                        .with(jwtFor(EGOR_USER_ID, "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].modelName", is("BMW 320i")));

        mockMvc.perform(get("/api/v1/cars/{id}", "40000000-0000-0000-0000-000000000001")
                        .with(jwtFor(EGOR_USER_ID, "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("40000000-0000-0000-0000-000000000001")));
    }

    private JwtRequestPostProcessor jwtFor(UUID appUserId, String... roles) {
        return jwt().jwt(jwt -> jwt
                .subject(appUserId.toString())
                .claim("app_user_id", appUserId.toString())
                .claim("realm_access", Map.of("roles", List.of(roles))))
                .authorities(Arrays.stream(roles)
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .map(GrantedAuthority.class::cast)
                        .toList());
    }

    private static final class FakeCarInventoryService extends CarInventoryServiceGrpc.CarInventoryServiceImplBase {
        @Override
        public void listAvailableCars(
                ListAvailableCarsRequest request,
                StreamObserver<AvailableCarsResponse> responseObserver
        ) {
            responseObserver.onNext(AvailableCarsResponse.newBuilder().addCars(car()).build());
            responseObserver.onCompleted();
        }

        @Override
        public void getAvailableCar(
                GetAvailableCarRequest request,
                StreamObserver<AvailableCarResponse> responseObserver
        ) {
            responseObserver.onNext(AvailableCarResponse.newBuilder().setFound(true).setCar(car()).build());
            responseObserver.onCompleted();
        }

        private AvailableCar car() {
            return AvailableCar.newBuilder()
                    .setId("40000000-0000-0000-0000-000000000001")
                    .setModelId("20000000-0000-0000-0000-000000000001")
                    .setModelName("BMW 320i")
                    .setColor("Black")
                    .setPrice("4050000.00")
                    .setAvailable(true)
                    .setTestDriveAvailable(false)
                    .build();
        }
    }
}
