package com.certiva.api.Util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.certiva.api.DTO.EventoDTO;
import com.certiva.api.Entity.Evento;
import com.certiva.api.Exception.OperacionNoPermitidaException;
import com.certiva.api.enums.EstadoOperativoEvento;

/**
 * Reglas de edición de eventos por rol y estado operativo (HU).
 */
public final class EventoEdicionPolicy {

    private EventoEdicionPolicy() {
    }

    public static void validarEdicion(Evento evento, EventoDTO dto, boolean esAdmin) {
        EstadoOperativoEvento operativo = EstadoOperativoEventoHelper.resolverOperativoVisible(
                evento, java.time.LocalDateTime.now());

        if (operativo == EstadoOperativoEvento.CERRADO_Y_CERTIFICADO) {
            throw new OperacionNoPermitidaException(
                    "El evento está cerrado y certificado; no admite modificaciones.");
        }
        if (operativo == EstadoOperativoEvento.EVENT_CANCELLED) {
            throw new OperacionNoPermitidaException("El evento está cancelado; no admite modificaciones.");
        }

        if (esAdmin && operativo == EstadoOperativoEvento.EN_CURSO) {
            validarSoloCambioPersonal(dto, evento);
            return;
        }

        if (!esAdmin && operativo != EstadoOperativoEvento.PROXIMO) {
            throw new OperacionNoPermitidaException(
                    "Solo puede editar el evento completo mientras está en estado Próximo.");
        }

        if (esAdmin && operativo != EstadoOperativoEvento.PROXIMO
                && operativo != EstadoOperativoEvento.EN_CURSO) {
            throw new OperacionNoPermitidaException(
                    "En este estado el administrador solo puede supervisar o forzar el cierre.");
        }
    }

    private static void validarSoloCambioPersonal(EventoDTO dto, Evento evento) {
        if (cambioDatosGenerales(dto, evento)) {
            throw new OperacionNoPermitidaException(
                    "Con el evento en curso el administrador solo puede reasignar profesores o monitores.");
        }
    }

    private static boolean cambioDatosGenerales(EventoDTO dto, Evento evento) {
        return distinto(dto.getNombreEvento(), evento.getNombreEvento())
                || distinto(dto.getDescripcion(), evento.getDescripcion())
                || distinto(dto.getModalidad(), evento.getModalidad())
                || distinto(dto.getFechaInicio(), evento.getFechaInicio())
                || distinto(dto.getFechaFin(), evento.getFechaFin())
                || distinto(dto.getUbicacion(), evento.getUbicacion())
                || distinto(dto.getEnlaceVirtual(), evento.getEnlaceVirtual())
                || distinto(dto.getAforoMaximo(), evento.getAforoMaximo())
                || distinto(dto.getIntensidadHoraria(), evento.getIntensidadHoraria())
                || distinto(dto.getPrecio(), evento.getCosto())
                || distinto(dto.getTextoDiploma(), evento.getTextoDiploma())
                || distinto(dto.getFirmaDigitalProfesor(), evento.getFirmaDigitalProfesor());
    }

    private static boolean distinto(Object a, Object b) {
        if (a == null && b == null) {
            return false;
        }
        if (a == null || b == null) {
            return true;
        }
        return !a.equals(b);
    }

    public static boolean esAdminAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }
}
