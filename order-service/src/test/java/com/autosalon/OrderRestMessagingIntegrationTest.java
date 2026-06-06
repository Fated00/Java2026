package com.autosalon;

import com.autosalon.messaging.OrderApprovalResultEvent;
import com.autosalon.messaging.OrderKind;
import com.autosalon.service.OrderApprovalResultListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderRestMessagingIntegrationTest extends OrderIntegrationTestBase {
    private static final UUID EGOR_USER_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID EGOR_OTHER_ID = UUID.fromString("10000000-0000-0000-0000-000000000005");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private OrderApprovalResultListener approvalResultListener;

    @Test
    void payingOrderCreatesOutboxEventAndApprovalUpdatesStatus() throws Exception {
        String response = mockMvc.perform(post("/api/orders/in-stock")
                        .with(jwtFor(EGOR_USER_ID, "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "carId": "40000000-0000-0000-0000-000000000001"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("CREATED")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String orderId = objectMapper.readTree(response).path("id").asText();

        mockMvc.perform(post("/api/orders/in-stock/{id}/pay", orderId)
                        .with(jwtFor(EGOR_USER_ID, "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("PAID")));

        Integer outboxEvents = jdbcTemplate.queryForObject(
                "select count(*) from outbox_events where aggregate_id = ? and status = 'NEW'",
                Integer.class,
                UUID.fromString(orderId)
        );
        assertThat(outboxEvents).isEqualTo(1);

        approvalResultListener.handle(new OrderApprovalResultEvent(UUID.fromString(orderId), OrderKind.IN_STOCK, true, null));

        mockMvc.perform(get("/api/orders/in-stock/{id}", orderId)
                        .with(jwtFor(EGOR_USER_ID, "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CAR_READY_FOR_PICKUP")));
    }

    @Test
    void userCannotReadAnotherUsersOrder() throws Exception {
        String response = mockMvc.perform(post("/api/orders/in-stock")
                        .with(jwtFor(EGOR_USER_ID, "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "carId": "40000000-0000-0000-0000-000000000002"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String orderId = objectMapper.readTree(response).path("id").asText();

        mockMvc.perform(get("/api/orders/in-stock/{id}", orderId)
                        .with(jwtFor(EGOR_OTHER_ID, "USER")))
                .andExpect(status().isForbidden());
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
