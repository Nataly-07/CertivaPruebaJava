package com.certiva.api.Util;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contenido del QR de inscripción: URL de validación atada al {@code id_inscripcion}.
 */
public final class InscripcionQrHelper {

    private static final Pattern INSCRIPCION_ID_PARAM =
            Pattern.compile("(?:\\?|&|#)inscripcionId=(\\d+)", Pattern.CASE_INSENSITIVE);

    private InscripcionQrHelper() {
    }

    public static String buildQrContent(String publicApiBaseUrl, Long idInscripcion) {
        if (idInscripcion == null) {
            return "";
        }
        String base = normalizarBase(publicApiBaseUrl);
        return base + "/api/asistencias/validar?inscripcionId=" + idInscripcion;
    }

    /**
     * Extrae el id de inscripción desde URL de validación, número plano o token legado.
     */
    public static Optional<Long> resolveInscripcionId(String codigoBruto) {
        if (codigoBruto == null || codigoBruto.isBlank()) {
            return Optional.empty();
        }
        String codigo = codigoBruto.trim();

        Matcher urlMatcher = INSCRIPCION_ID_PARAM.matcher(codigo);
        if (urlMatcher.find()) {
            return Optional.of(Long.parseLong(urlMatcher.group(1)));
        }

        if (codigo.matches("\\d+")) {
            return Optional.of(Long.parseLong(codigo));
        }

        return Optional.empty();
    }

    private static String normalizarBase(String publicApiBaseUrl) {
        if (publicApiBaseUrl == null || publicApiBaseUrl.isBlank()) {
            return "http://localhost:8080";
        }
        String base = publicApiBaseUrl.trim();
        if (base.endsWith("/api")) {
            base = base.substring(0, base.length() - 4);
        }
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base;
    }
}
