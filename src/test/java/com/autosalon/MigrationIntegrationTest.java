package com.autosalon;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class MigrationIntegrationTest extends IntegrationTestBase {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void liquibaseCreatesSchemaAndSeedsData() {
        Integer migrationCount = jdbcTemplate.queryForObject(
                "select count(*) from databasechangelog",
                Integer.class
        );
        Integer userCount = jdbcTemplate.queryForObject("select count(*) from app_users", Integer.class);
        Integer modelCount = jdbcTemplate.queryForObject("select count(*) from car_models", Integer.class);
        Integer baseComponentCount = jdbcTemplate.queryForObject(
                "select count(*) from car_model_base_component_options",
                Integer.class
        );

        assertThat(migrationCount).isGreaterThanOrEqualTo(2);
        assertThat(userCount).isEqualTo(5);
        assertThat(modelCount).isEqualTo(3);
        assertThat(baseComponentCount).isEqualTo(12);
    }

    @Test
    void baseEntityColumnsExistForJpaEntities() {
        Integer auditColumnCount = jdbcTemplate.queryForObject(
                """
                        select count(*)
                        from information_schema.columns
                        where table_name = 'cars'
                          and column_name in ('id', 'created_at', 'updated_at', 'removed')
                        """,
                Integer.class
        );

        assertThat(auditColumnCount).isEqualTo(4);
    }
}
