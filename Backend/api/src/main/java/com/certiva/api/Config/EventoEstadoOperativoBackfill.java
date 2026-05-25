package com.certiva.api.Config;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.certiva.api.Entity.Evento;
import com.certiva.api.Repository.EventoRepository;
import com.certiva.api.Util.EstadoOperativoEventoHelper;
import com.certiva.api.enums.EstadoOperativoEvento;

/**
 * Debe ejecutarse antes que {@link EventoCatalogoBackfill} (columna + datos para JPA).
 */
@Component
@Order(1)
public class EventoEstadoOperativoBackfill implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(EventoEstadoOperativoBackfill.class);

    private final EventoSchemaMigrationHelper schemaMigration;
    private final EventoRepository eventoRepository;
    private final TransactionTemplate txNueva;

    public EventoEstadoOperativoBackfill(EventoSchemaMigrationHelper schemaMigration,
                                         EventoRepository eventoRepository,
                                         PlatformTransactionManager transactionManager) {
        this.schemaMigration = schemaMigration;
        this.eventoRepository = eventoRepository;
        this.txNueva = new TransactionTemplate(transactionManager);
        this.txNueva.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public void run(String... args) {
        try {
            txNueva.executeWithoutResult(status -> {
                schemaMigration.asegurarColumnaEstadoOperativo();
                rellenarEstados();
            });
        } catch (Exception ex) {
            log.warn("Backfill estado_operativo: {}", ex.getMessage());
        }
    }

    private void rellenarEstados() {
        LocalDateTime ahora = LocalDateTime.now();
        List<Evento> todos = eventoRepository.findAll();
        for (Evento e : todos) {
            if (Boolean.FALSE.equals(e.getEstado())) {
                e.setEstadoOperativo(EstadoOperativoEvento.EVENT_CANCELLED);
            } else if (e.getEstadoOperativo() == null) {
                e.setEstadoOperativo(EstadoOperativoEventoHelper.calcularPorReloj(
                        e.getFechaInicio(), e.getFechaFin(), ahora));
            }
            eventoRepository.save(e);
        }
        if (!todos.isEmpty()) {
            log.info("Backfill estado_operativo revisado en {} evento(s)", todos.size());
        }
    }
}
