package com.certiva.api.Util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Normaliza rutas locales de uploads y URLs externas de imagen promocional.
 * Alineado con {@code evento-imagen.util.ts} del frontend.
 */
public final class ImagenPromocionalHelper {

    private static final Pattern DRIVE_FILE_ID = Pattern.compile(
            "drive\\.google\\.com/(?:file/d/|open\\?id=)([a-zA-Z0-9_-]+)",
            Pattern.CASE_INSENSITIVE);

    private ImagenPromocionalHelper() {
    }

    public static String normalizar(String ruta) {
        if (ruta == null || ruta.isBlank()) {
            return null;
        }
        String r = ruta.trim().replaceAll("\\s+", "");

        if (r.startsWith("data:")) {
            return r;
        }
        if (r.startsWith("//")) {
            return convertirEnlaceCompartido("https:" + r);
        }
        if (r.regionMatches(true, 0, "http://", 0, 7) || r.regionMatches(true, 0, "https://", 0, 8)) {
            return convertirEnlaceCompartido(r);
        }

        r = quitarPrefijoApiLocal(r);
        if (r.startsWith("/uploads/")) {
            r = r.substring("/uploads/".length());
        } else if (r.startsWith("uploads/")) {
            r = r.substring("uploads/".length());
        }

        if (esRutaArchivoSubido(r)) {
            return r.startsWith("/") ? r.substring(1) : r;
        }

        if (pareceHostWeb(r)) {
            return convertirEnlaceCompartido("https://" + r.replaceFirst("^/+", ""));
        }

        return r.startsWith("/") ? r.substring(1) : r;
    }

    private static String quitarPrefijoApiLocal(String r) {
        String sinApi = r;
        for (String prefijo : new String[] { "http://localhost:8080", "http://127.0.0.1:8080" }) {
            if (sinApi.startsWith(prefijo)) {
                sinApi = sinApi.substring(prefijo.length());
                break;
            }
        }
        return sinApi.startsWith("/") ? sinApi.substring(1) : sinApi;
    }

    private static boolean esRutaArchivoSubido(String r) {
        String rel = r.startsWith("/") ? r.substring(1) : r;
        return rel.regionMatches(true, 0, "imagenes/", 0, "imagenes/".length())
                || rel.regionMatches(true, 0, "pensum/", 0, "pensum/".length())
                || rel.regionMatches(true, 0, "uploads/", 0, "uploads/".length());
    }

    private static boolean pareceHostWeb(String valor) {
        try {
            String probe = valor.contains("://") ? valor : "https://" + valor;
            URI uri = new URI(probe);
            String host = uri.getHost();
            if (host == null || host.isBlank()) {
                return false;
            }
            return host.contains(".") || "localhost".equalsIgnoreCase(host) || host.matches("\\d{1,3}(\\.\\d{1,3}){3}");
        } catch (URISyntaxException ex) {
            return false;
        }
    }

    private static String convertirEnlaceCompartido(String url) {
        Matcher drive = DRIVE_FILE_ID.matcher(url);
        if (drive.find()) {
            return "https://drive.google.com/uc?export=view&id=" + drive.group(1);
        }
        if (url.contains("dropbox.com")) {
            String out = url.replace("www.dropbox.com", "dl.dropboxusercontent.com");
            if (out.contains("dl=0")) {
                out = out.replace("dl=0", "raw=1");
            } else if (!out.contains("raw=1")) {
                out = out + (out.contains("?") ? "&" : "?") + "raw=1";
            }
            return out;
        }
        return url;
    }
}
