package com.certiva.api.Util;

import java.util.Set;

public final class InscripcionEstadoHelper {

    public static final String APROBADA = "APROBADA";
    public static final String PENDIENTE = "PENDIENTE";
    public static final String CANCELLED = "CANCELLED";
    /** Legado; tratado como cancelación. */
    public static final String INACTIVO = "INACTIVO";
    public static final String PRESENTE = "PRESENTE";
    public static final String TARDIO = "TARDIO";
    public static final String AUSENTE = "AUSENTE";
    /** Legado de asistencia confirmada. */
    public static final String ASISTIO = "ASISTIO";

    private static final Set<String> OCUPAN_CUPO = Set.of(APROBADA, PENDIENTE, PRESENTE, TARDIO, AUSENTE, ASISTIO);

    private static final Set<String> ASISTENCIA_CONFIRMADA = Set.of(PRESENTE, TARDIO, ASISTIO);

    private InscripcionEstadoHelper() {
    }

    public static String norm(String estado) {
        return estado == null ? "" : estado.trim().toUpperCase();
    }

    public static boolean ocupaCupo(String estado) {
        return OCUPAN_CUPO.contains(norm(estado));
    }

    public static boolean esCancelada(String estado) {
        String n = norm(estado);
        return CANCELLED.equals(n) || INACTIVO.equals(n);
    }

    public static boolean tieneAsistenciaConfirmada(String estado) {
        return ASISTENCIA_CONFIRMADA.contains(norm(estado));
    }

    public static boolean puedeInscribirseEnEvento(String estadoInscripcion) {
        return !esCancelada(estadoInscripcion);
    }
}
