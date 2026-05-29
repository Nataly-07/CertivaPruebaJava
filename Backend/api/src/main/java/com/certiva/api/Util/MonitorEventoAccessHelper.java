package com.certiva.api.Util;

import com.certiva.api.Entity.Evento;
import com.certiva.api.Entity.Usuario;
import com.certiva.api.Exception.OperacionNoPermitidaException;

public final class MonitorEventoAccessHelper {

    private MonitorEventoAccessHelper() {
    }

    public static void asegurarMonitorEvento(Evento evento, Usuario monitor) {
        if (EventoEdicionPolicy.esAdminAutenticado()) {
            return;
        }
        if (monitor == null || monitor.getIdUsuario() == null) {
            throw new OperacionNoPermitidaException("No se pudo identificar al usuario.");
        }
        Long id = monitor.getIdUsuario();
        boolean asignado = evento.getMonitoresAsignados() != null
                && evento.getMonitoresAsignados().stream()
                        .anyMatch(m -> id.equals(m.getIdUsuario()));
        if (!asignado) {
            throw new OperacionNoPermitidaException(
                    "No está asignado como monitor de este evento. "
                            + "Solicite al organizador que lo vincule al evento.");
        }
    }
}
