package com.certiva.api.Util;

import com.certiva.api.Entity.Evento;
import com.certiva.api.Entity.Usuario;
import com.certiva.api.Exception.OperacionNoPermitidaException;

/**
 * El profesor solo gestiona eventos donde el organizador lo asignó como colaborador.
 * No crea eventos globales ni modifica configuración general del sistema.
 */
public final class ProfesorEventoAccessHelper {

    private ProfesorEventoAccessHelper() {
    }

    public static void asegurarGestionEvento(Evento evento, Usuario profesor) {
        if (EventoEdicionPolicy.esAdminAutenticado()) {
            return;
        }
        if (profesor == null || profesor.getIdUsuario() == null) {
            throw new OperacionNoPermitidaException("No se pudo identificar al usuario.");
        }
        Long id = profesor.getIdUsuario();
        boolean colaborador = evento.getProfesoresColaboradores() != null
                && evento.getProfesoresColaboradores().stream()
                        .anyMatch(p -> id.equals(p.getIdUsuario()));
        if (!colaborador) {
            throw new OperacionNoPermitidaException(
                    "No está asignado como profesor colaborador de este evento. "
                            + "Solicite al organizador que lo vincule al evento.");
        }
    }
}
