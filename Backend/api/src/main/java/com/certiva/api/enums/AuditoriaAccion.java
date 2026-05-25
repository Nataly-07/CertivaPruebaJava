package com.certiva.api.enums;

/**
 * Códigos de auditoría de dominio (contrato ROOM_911 / HU eventos).
 */
public final class AuditoriaAccion {

    public static final String ROLE_CHANGE = "ROLE_CHANGE";
    public static final String EVENT_CREATED = "EVENT_CREATED";
    public static final String CHECKIN_SUCCESS = "CHECKIN_SUCCESS";
    public static final String CHECKIN_DENIED = "CHECKIN_DENIED";
    public static final String EVENT_CLOSED = "EVENT_CLOSED";
    public static final String CERTIFICATE_GENERATED = "CERTIFICATE_GENERATED";
    public static final String EVENT_CANCELLED = "EVENT_CANCELLED";
    public static final String EVENT_REVISION_STARTED = "EVENT_REVISION_STARTED";
    public static final String EVENT_FORCE_CLOSED = "EVENT_FORCE_CLOSED";
    public static final String INSCRIPCION_CANCELLED = "INSCRIPCION_CANCELLED";

    private AuditoriaAccion() {
    }
}
