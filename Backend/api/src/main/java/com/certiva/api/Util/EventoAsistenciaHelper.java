package com.certiva.api.Util;

import com.certiva.api.Entity.Evento;
import com.certiva.api.Exception.OperacionNoPermitidaException;

public final class EventoAsistenciaHelper {

    public static final int PORCENTAJE_MINIMO_DEFECTO = 80;

    private EventoAsistenciaHelper() {
    }

    public static int resolverPorcentajeMinimo(Evento evento) {
        if (evento == null) {
            return PORCENTAJE_MINIMO_DEFECTO;
        }
        Integer v = evento.getPorcentajeAsistenciaMinimo();
        if (v == null || v < 1) {
            return PORCENTAJE_MINIMO_DEFECTO;
        }
        return Math.min(100, v);
    }

    public static void validarPorcentajeMinimo(Integer porcentaje) {
        if (porcentaje == null) {
            throw new OperacionNoPermitidaException(
                    "El porcentaje de asistencia mínima es obligatorio para certificar.");
        }
        if (porcentaje < 1 || porcentaje > 100) {
            throw new OperacionNoPermitidaException(
                    "La asistencia mínima debe estar entre 1 y 100 %.");
        }
    }
}
