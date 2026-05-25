package com.certiva.api.Config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * DDL idempotente para columnas de migración en {@code evento}.
 */
@Component
public class EventoSchemaMigrationHelper {

    private final JdbcTemplate jdbcTemplate;

    public EventoSchemaMigrationHelper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void asegurarColumnaEstadoOperativo() {
        jdbcTemplate.execute("""
                ALTER TABLE evento
                ADD COLUMN IF NOT EXISTS estado_operativo VARCHAR(40)
                """);
        jdbcTemplate.execute("""
                UPDATE evento SET estado_operativo = 'PROXIMO' WHERE estado_operativo IS NULL
                """);
    }
}
