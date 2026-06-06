package com.autosalon;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMigrationIntegrationTest extends OrderIntegrationTestBase {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void appliesOrderServiceMigrations() {
        Integer users = jdbcTemplate.queryForObject("select count(*) from app_users", Integer.class);
        Integer outboxEvents = jdbcTemplate.queryForObject("select count(*) from outbox_events", Integer.class);

        assertThat(users).isEqualTo(5);
        assertThat(outboxEvents).isNotNegative();
    }
}
