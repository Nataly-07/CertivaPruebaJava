package com.certiva.api.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.certiva.api.Service.EventoCicloVidaService;

@Component
public class EventoCicloVidaScheduler {

    private static final Logger log = LoggerFactory.getLogger(EventoCicloVidaScheduler.class);

    private final EventoCicloVidaService eventoCicloVidaService;

    public EventoCicloVidaScheduler(EventoCicloVidaService eventoCicloVidaService) {
        this.eventoCicloVidaService = eventoCicloVidaService;
    }

    /** Transiciones automáticas Próximo → En curso → Finalizado por tiempo. */
    @Scheduled(fixedRate = 60_000)
    public void sincronizarEstados() {
        try {
            eventoCicloVidaService.sincronizarEstadosAutomaticos();
        } catch (Exception ex) {
            log.debug("Scheduler estado operativo: {}", ex.getMessage());
        }
    }
}
