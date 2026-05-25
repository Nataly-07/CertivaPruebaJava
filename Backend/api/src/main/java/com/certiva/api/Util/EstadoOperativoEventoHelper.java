package com.certiva.api.Util;

import java.time.LocalDateTime;

import com.certiva.api.Entity.Evento;
import com.certiva.api.enums.EstadoOperativoEvento;

public final class EstadoOperativoEventoHelper {

    private EstadoOperativoEventoHelper() {
    }

    public static boolean esTerminal(EstadoOperativoEvento estado) {
        return estado == EstadoOperativoEvento.CERRADO_Y_CERTIFICADO
                || estado == EstadoOperativoEvento.EVENT_CANCELLED;
    }

    public static boolean admiteTransicionAutomatica(EstadoOperativoEvento estado) {
        return estado == EstadoOperativoEvento.PROXIMO
                || estado == EstadoOperativoEvento.EN_CURSO
                || estado == EstadoOperativoEvento.FINALIZADO_POR_TIEMPO;
    }

    /**
     * Calcula el estado según reloj del servidor (Próximo → En curso → Finalizado por tiempo).
     */
    public static EstadoOperativoEvento calcularPorReloj(LocalDateTime inicio, LocalDateTime fin, LocalDateTime ahora) {
        if (inicio == null || fin == null) {
            return EstadoOperativoEvento.PROXIMO;
        }
        if (ahora.isBefore(inicio)) {
            return EstadoOperativoEvento.PROXIMO;
        }
        if (!ahora.isAfter(fin)) {
            return EstadoOperativoEvento.EN_CURSO;
        }
        return EstadoOperativoEvento.FINALIZADO_POR_TIEMPO;
    }

    public static EstadoOperativoEvento resolverOperativoVisible(Evento evento, LocalDateTime ahora) {
        if (evento == null) {
            return EstadoOperativoEvento.PROXIMO;
        }
        EstadoOperativoEvento guardado = evento.getEstadoOperativo();
        if (guardado == null) {
            return calcularPorReloj(evento.getFechaInicio(), evento.getFechaFin(), ahora);
        }
        if (guardado == EstadoOperativoEvento.EVENT_CANCELLED
                || guardado == EstadoOperativoEvento.CERRADO_Y_CERTIFICADO
                || guardado == EstadoOperativoEvento.EN_REVISION) {
            return guardado;
        }
        if (!Boolean.TRUE.equals(evento.getEstado())) {
            return EstadoOperativoEvento.EVENT_CANCELLED;
        }
        if (admiteTransicionAutomatica(guardado)) {
            return calcularPorReloj(evento.getFechaInicio(), evento.getFechaFin(), ahora);
        }
        return guardado;
    }

    public static EstadoOperativoEvento sincronizarAutomatico(Evento evento, LocalDateTime ahora) {
        if (evento == null || evento.getEstadoOperativo() == null) {
            return EstadoOperativoEvento.PROXIMO;
        }
        if (!Boolean.TRUE.equals(evento.getEstado())) {
            evento.setEstadoOperativo(EstadoOperativoEvento.EVENT_CANCELLED);
            return EstadoOperativoEvento.EVENT_CANCELLED;
        }
        EstadoOperativoEvento actual = evento.getEstadoOperativo();
        if (!admiteTransicionAutomatica(actual)) {
            return actual;
        }
        EstadoOperativoEvento calculado = calcularPorReloj(evento.getFechaInicio(), evento.getFechaFin(), ahora);
        evento.setEstadoOperativo(calculado);
        return calculado;
    }
}
