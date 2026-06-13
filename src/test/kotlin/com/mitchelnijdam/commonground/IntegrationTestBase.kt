package com.mitchelnijdam.commonground

import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer

/**
 * Base for integration tests: boots the full application against a shared Postgres Testcontainer.
 * The container is started once and reused by every test class extending this base; the schema is
 * wiped before each test so state never leaks between tests or Spring contexts.
 */
@SpringBootTest
abstract class IntegrationTestBase {

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun truncateAllTables() {
        val tables = jdbcTemplate.queryForList(
            "select tablename from pg_tables where schemaname = 'common_ground' and tablename <> 'flyway_schema_history'",
            String::class.java,
        )
        if (tables.isNotEmpty()) {
            jdbcTemplate.execute(
                "truncate table ${tables.joinToString { "common_ground.\"$it\"" }} restart identity cascade",
            )
        }
    }

    companion object {
        private val postgres = PostgreSQLContainer("postgres:16-alpine").apply { start() }

        @JvmStatic
        @DynamicPropertySource
        fun datasourceProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }
}
