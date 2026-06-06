package com.autosalon;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StorageMigrationRestIntegrationTest extends StorageIntegrationTestBase {
    private static final UUID EGOR_USER_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void appliesStorageMigrations() {
        Integer cars = jdbcTemplate.queryForObject("select count(*) from cars", Integer.class);
        Integer assemblyOrders = jdbcTemplate.queryForObject("select count(*) from assembly_orders", Integer.class);

        assertThat(cars).isEqualTo(2);
        assertThat(assemblyOrders).isZero();
    }

    @Test
    void exposesSecuredAvailableCarsApi() throws Exception {
        mockMvc.perform(get("/api/catalog/cars/available")
                        .with(jwtFor(EGOR_USER_ID, "USER"))
                        .param("brand", "BMW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].modelName", is("BMW 320i")));
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
