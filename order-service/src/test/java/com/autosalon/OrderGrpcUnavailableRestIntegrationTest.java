package com.autosalon;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OrderGrpcUnavailableRestIntegrationTest extends OrderIntegrationTestBase {
    private static final UUID EGOR_USER_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final int UNUSED_GRPC_PORT = unusedPort();

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void registerGrpcProperties(DynamicPropertyRegistry registry) {
        registry.add("autosalon.grpc.storage.host", () -> "localhost");
        registry.add("autosalon.grpc.storage.port", () -> UNUSED_GRPC_PORT);
        registry.add("autosalon.grpc.storage.timeout-ms", () -> "100");
    }

    @Test
    void returns503WhenStorageGrpcIsUnavailable() throws Exception {
        mockMvc.perform(get("/api/v1/cars")
                        .with(jwtFor(EGOR_USER_ID, "USER")))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code", is("STORAGE_SERVICE_UNAVAILABLE")));
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

    private static int unusedPort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
