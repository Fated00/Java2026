package com.autosalon;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RestApiIntegrationTest extends IntegrationTestBase {
    private static final UUID EGOR_USER_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID EGOR_OTHER_ID = UUID.fromString("10000000-0000-0000-0000-000000000005");
    private static final UUID EGOR_MANAGER_ID = UUID.fromString("10000000-0000-0000-0000-000000000002");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void swaggerUiIsAvailable() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));
    }

    @Test
    void filtersAvailableCarsThroughRestApi() throws Exception {
        mockMvc.perform(get("/api/catalog/cars/available")
                        .with(jwtFor(EGOR_USER_ID, "USER"))
                        .param("brand", "BMW")
                        .param("model", "320i")
                        .param("color", "Black"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].modelName", is("BMW 320i")))
                .andExpect(jsonPath("$[0].available", is(true)));
    }

    @Test
    void buildsConfigurationThroughRestApi() throws Exception {
        String request = """
                {
                  "modelId": "20000000-0000-0000-0000-000000000001",
                  "selectedOptionIds": {
                    "WHEELS": "30000000-0000-0000-0000-000000000002",
                    "TRANSMISSION": "30000000-0000-0000-0000-000000000004",
                    "STEERING_WHEEL": "30000000-0000-0000-0000-000000000007",
                    "INTERIOR": "30000000-0000-0000-0000-000000000009"
                  }
                }
                """;

        mockMvc.perform(post("/api/configurator/configurations")
                        .with(jwtFor(EGOR_USER_ID, "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelName", is("BMW 320i")))
                .andExpect(jsonPath("$.totalPrice", is(4230000.0)));
    }

    @Test
    void createsCustomOrderThroughRestApi() throws Exception {
        String request = """
                {
                  "modelId": "20000000-0000-0000-0000-000000000001",
                  "selectedOptionIds": {
                    "WHEELS": "30000000-0000-0000-0000-000000000002",
                    "TRANSMISSION": "30000000-0000-0000-0000-000000000004",
                    "STEERING_WHEEL": "30000000-0000-0000-0000-000000000007",
                    "INTERIOR": "30000000-0000-0000-0000-000000000009"
                  }
                }
                """;

        mockMvc.perform(post("/api/orders/custom")
                        .with(jwtFor(EGOR_USER_ID, "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("CREATED")))
                .andExpect(jsonPath("$.configuration.totalPrice", is(4230000.0)));
    }

    @Test
    void createsTestDriveRequestThroughRestApi() throws Exception {
        String request = """
                {
                  "carId": "40000000-0000-0000-0000-000000000002",
                  "startsAt": "2030-06-07T12:00:00"
                }
                """;

        mockMvc.perform(post("/api/test-drives")
                        .with(jwtFor(EGOR_USER_ID, "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.carId", is("40000000-0000-0000-0000-000000000002")));
    }

    @Test
    void rejectsUnauthenticatedApiRequest() throws Exception {
        mockMvc.perform(get("/api/catalog/cars/available"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsWarehouseMutationForUserRole() throws Exception {
        String request = """
                {
                  "modelId": "20000000-0000-0000-0000-000000000001",
                  "color": "Green",
                  "price": 4100000
                }
                """;

        mockMvc.perform(post("/api/catalog/cars")
                        .with(jwtFor(EGOR_USER_ID, "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isForbidden());
    }

    @Test
    void userCannotReadAnotherUsersOrderButManagerCan() throws Exception {
        String request = """
                {
                  "modelId": "20000000-0000-0000-0000-000000000001",
                  "selectedOptionIds": {
                    "WHEELS": "30000000-0000-0000-0000-000000000002",
                    "TRANSMISSION": "30000000-0000-0000-0000-000000000004",
                    "STEERING_WHEEL": "30000000-0000-0000-0000-000000000007",
                    "INTERIOR": "30000000-0000-0000-0000-000000000009"
                  }
                }
                """;

        String response = mockMvc.perform(post("/api/orders/custom")
                        .with(jwtFor(EGOR_USER_ID, "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String orderId = objectMapper.readTree(response).path("id").asText();

        mockMvc.perform(get("/api/orders/custom/{id}", orderId)
                        .with(jwtFor(EGOR_OTHER_ID, "USER")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/orders/custom/{id}", orderId)
                        .with(jwtFor(EGOR_MANAGER_ID, "MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(orderId)));
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
}
