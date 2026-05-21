package com.certiva.api.Config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Diagnóstico al arranque: confirma tablas críticas en PostgreSQL.
 */
@Component
public class DatabaseSchemaLogger {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSchemaLogger.class);

    private final JdbcTemplate jdbcTemplate;

    public DatabaseSchemaLogger(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logTablasCriticas() {
        try {
            List<String> tablas = jdbcTemplate.queryForList(
                    """
                    SELECT table_name FROM information_schema.tables
                    WHERE table_schema = 'public'
                    ORDER BY table_name
                    """,
                    String.class);
            log.info("PostgreSQL conectado. Tablas public ({}): {}", tablas.size(), tablas);
            for (String requerida : List.of("usuario", "evento", "inscripcion", "rol")) {
                boolean ok = tablas.stream().anyMatch(t -> t.equalsIgnoreCase(requerida));
                if (!ok) {
                    log.warn("Tabla esperada '{}' no encontrada en public.", requerida);
                }
            }
        } catch (Exception ex) {
            log.error("No se pudo leer el esquema de PostgreSQL: {}", ex.getMessage());
        }
    }
}
