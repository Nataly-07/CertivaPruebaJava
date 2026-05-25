package com.certiva.api.Util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.certiva.api.Entity.Evento;
import com.certiva.api.Entity.Inscripcion;

public final class ProfesorAsistenciaHelper {

    private ProfesorAsistenciaHelper() {
    }

    public static int porcentajeAsistenciaEstudiante(Inscripcion inscripcion, Evento evento, LocalDateTime ahora) {
        int total = calcularSesionesTotales(evento);
        if (total <= 0) {
            return 0;
        }
        int asistidas = calcularSesionesAsistidas(inscripcion, evento, ahora, total);
        return Math.min(100, Math.round((asistidas * 100f) / total));
    }

    public static int porcentajeAsistenciaGlobal(long asistenciasConfirmadas, long inscritosActivos) {
        if (inscritosActivos <= 0) {
            return 0;
        }
        return Math.min(100, Math.round((asistenciasConfirmadas * 100f) / inscritosActivos));
    }

    private static int calcularSesionesTotales(Evento evento) {
        if (evento.getIntensidadHoraria() != null && evento.getIntensidadHoraria() > 0) {
            return Math.max(1, (int) Math.ceil(evento.getIntensidadHoraria() / 4.0));
        }
        if (evento.getFechaInicio() != null && evento.getFechaFin() != null) {
            long dias = ChronoUnit.DAYS.between(
                    evento.getFechaInicio().toLocalDate(),
                    evento.getFechaFin().toLocalDate()) + 1;
            return (int) Math.max(1, Math.min(dias, 40));
        }
        return 1;
    }

    private static int calcularSesionesAsistidas(Inscripcion inscripcion, Evento evento,
                                                 LocalDateTime ahora, int sesionesTotales) {
        if (InscripcionEstadoHelper.tieneAsistenciaConfirmada(inscripcion.getEstado())) {
            return sesionesTotales;
        }
        if (evento.getFechaInicio() == null || evento.getFechaFin() == null) {
            return 0;
        }
        if (ahora.isBefore(evento.getFechaInicio())) {
            return 0;
        }
        if (!ahora.isBefore(evento.getFechaFin())) {
            return 0;
        }
        long totalMs = Duration.between(evento.getFechaInicio(), evento.getFechaFin()).toMillis();
        if (totalMs <= 0) {
            return 0;
        }
        long elapsedMs = Duration.between(evento.getFechaInicio(), ahora).toMillis();
        int estimado = (int) Math.round((elapsedMs * (double) sesionesTotales) / totalMs);
        return Math.max(0, Math.min(sesionesTotales, estimado));
    }
}
