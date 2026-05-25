package com.certiva.api.Config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.certiva.api.Entity.Evento;
import com.certiva.api.Repository.EventoRepository;
import com.certiva.api.Util.TipoEventoCatalogoHelper;
import com.certiva.api.enums.TipoEventoEnum;

/**
 * Rellena {@code id_tipo_evento} y filas hijas JOINED en eventos legados.
 */
@Component
@Order(2)
public class EventoCatalogoBackfill implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(EventoCatalogoBackfill.class);

    private final EventoRepository eventoRepository;
    private final TipoEventoCatalogoHelper catalogoHelper;
    private final JdbcTemplate jdbcTemplate;
    private final EventoSchemaMigrationHelper schemaMigration;
    private final TransactionTemplate txNueva;

    public EventoCatalogoBackfill(EventoRepository eventoRepository,
                                  TipoEventoCatalogoHelper catalogoHelper,
                                  JdbcTemplate jdbcTemplate,
                                  EventoSchemaMigrationHelper schemaMigration,
                                  PlatformTransactionManager transactionManager) {
        this.eventoRepository = eventoRepository;
        this.catalogoHelper = catalogoHelper;
        this.jdbcTemplate = jdbcTemplate;
        this.schemaMigration = schemaMigration;
        this.txNueva = new TransactionTemplate(transactionManager);
        this.txNueva.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public void run(String... args) {
        try {
            txNueva.executeWithoutResult(status -> ejecutarBackfill());
        } catch (Exception ex) {
            log.error(
                    "Backfill de catálogo falló; el API sigue activo. Revise columnas fecha_inicio/fecha_fin y tabla usuario: {}",
                    ex.getMessage(),
                    ex);
        }
    }

    private void ejecutarBackfill() {
        schemaMigration.asegurarColumnaEstadoOperativo();
        catalogoHelper.asegurarCatalogoBase();
        asegurarColumnaTipoEventoEnEvento();
        repararFilasHijasJoined();
        rellenarCatalogoEnEventos();
        intentarRestriccionNotNull();
    }

    /** Eventos legados sin discriminante en columna tipo_evento. */
    private void asegurarColumnaTipoEventoEnEvento() {
        try {
            int n = jdbcTemplate.update("""
                    UPDATE evento
                    SET tipo_evento = 'CURSO'
                    WHERE tipo_evento IS NULL
                    """);
            if (n > 0) {
                log.info("tipo_evento por defecto (CURSO) en {} fila(s) de evento.", n);
            }
        } catch (Exception ex) {
            log.debug("No se pudo normalizar tipo_evento: {}", ex.getMessage());
        }
    }

    private void rellenarCatalogoEnEventos() {
        List<Evento> pendientes = eventoRepository.findByCatalogoTipoEventoIsNull();
        if (pendientes.isEmpty()) {
            return;
        }
        log.info("Rellenando id_tipo_evento en {} evento(s) existente(s)...", pendientes.size());
        int actualizados = 0;
        for (Evento evento : pendientes) {
            if (evento.getTipoEvento() == null) {
                log.warn("Evento id={} sin tipo_evento; se asigna CURSO por defecto.", evento.getIdEvento());
                evento.setTipoEvento(TipoEventoEnum.CURSO);
            }
            catalogoHelper.asignarCatalogoSiFalta(evento);
            eventoRepository.save(evento);
            actualizados++;
        }
        log.info("Backfill id_tipo_evento completado: {} evento(s) actualizado(s).", actualizados);
    }

    /**
     * Eventos guardados solo en {@code evento} sin fila en tablas hijas provocan 500 al listar con JOINED.
     */
    private void repararFilasHijasJoined() {
        int cursos = jdbcTemplate.update("""
                INSERT INTO curso_evento (id_evento, nivel_academico, nota_minima_aprobacion, porcentaje_asistencia_minimo)
                SELECT e.id_evento, 'BASICO', 3.0, 80
                FROM evento e
                WHERE UPPER(TRIM(CAST(e.tipo_evento AS varchar))) = 'CURSO'
                  AND NOT EXISTS (SELECT 1 FROM curso_evento c WHERE c.id_evento = e.id_evento)
                """);
        int hacks = jdbcTemplate.update("""
                INSERT INTO hackathon_evento (id_evento, reto_tecnico, min_integrantes, max_integrantes)
                SELECT e.id_evento, 'Reto por definir', 2, 5
                FROM evento e
                WHERE UPPER(TRIM(CAST(e.tipo_evento AS varchar))) = 'HACKATHON'
                  AND NOT EXISTS (SELECT 1 FROM hackathon_evento h WHERE h.id_evento = e.id_evento)
                """);
        int talleres = jdbcTemplate.update("""
                INSERT INTO taller_evento (id_evento, material_guia)
                SELECT e.id_evento, NULL
                FROM evento e
                WHERE UPPER(TRIM(CAST(e.tipo_evento AS varchar))) = 'TALLER'
                  AND NOT EXISTS (SELECT 1 FROM taller_evento t WHERE t.id_evento = e.id_evento)
                """);
        int ferias = jdbcTemplate.update("""
                INSERT INTO feria_proyecto_evento (id_evento, categoria_exhibicion, stack_tecnologico, criterios_evaluacion)
                SELECT e.id_evento, 'SOFTWARE', '[]', 'Por definir'
                FROM evento e
                WHERE UPPER(TRIM(CAST(e.tipo_evento AS varchar))) = 'FERIA'
                  AND NOT EXISTS (SELECT 1 FROM feria_proyecto_evento f WHERE f.id_evento = e.id_evento)
                """);
        if (cursos + hacks + talleres + ferias > 0) {
            log.info("Filas JOINED reparadas: curso={}, hackathon={}, taller={}, feria={}",
                    cursos, hacks, talleres, ferias);
        }
    }

    private void intentarRestriccionNotNull() {
        try {
            Long nulos = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM evento WHERE id_tipo_evento IS NULL",
                    Long.class);
            if (nulos != null && nulos > 0) {
                log.warn("Quedan {} evento(s) sin id_tipo_evento; no se aplica NOT NULL.", nulos);
                return;
            }
            jdbcTemplate.execute("ALTER TABLE evento ALTER COLUMN id_tipo_evento SET NOT NULL");
            log.debug("Columna evento.id_tipo_evento marcada como NOT NULL.");
        } catch (Exception ex) {
            log.debug("No se aplicó NOT NULL en id_tipo_evento: {}", ex.getMessage());
        }
    }
}
