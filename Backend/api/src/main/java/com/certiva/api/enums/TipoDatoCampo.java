package com.certiva.api.enums;

/**
 * Tipo de dato de un campo personalizado del formulario de inscripción por evento.
 */
public enum TipoDatoCampo {
    TEXTO,
    NUMERO,
    SELECT,
    CHECKBOX,
    /** Enlace web (Meet, Zoom, portafolio, etc.). */
    URL,
    /** Imagen en base64 (data URL) o URL pública a imagen. */
    IMAGEN
}
