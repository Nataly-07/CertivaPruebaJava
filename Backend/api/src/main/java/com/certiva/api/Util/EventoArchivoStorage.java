package com.certiva.api.Util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.certiva.api.Exception.OperacionNoPermitidaException;

@Component
public class EventoArchivoStorage {

    private static final Set<String> IMAGENES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final Set<String> DOCUMENTOS = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

    private final Path baseDir;

    public EventoArchivoStorage(@Value("${certiva.upload.dir:uploads}") String uploadDir) {
        this.baseDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public String guardarImagen(MultipartFile file) {
        validarTipo(file, IMAGENES, "La imagen debe ser JPEG, PNG, WebP o GIF.");
        return guardar(file, "imagenes");
    }

    public String guardarPensum(MultipartFile file) {
        validarTipo(file, DOCUMENTOS, "El pensum debe ser PDF o Word.");
        return guardar(file, "pensum");
    }

    private String guardar(MultipartFile file, String subcarpeta) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            Path dir = baseDir.resolve(subcarpeta);
            Files.createDirectories(dir);
            String ext = extensionDesdeNombre(file.getOriginalFilename());
            String nombre = UUID.randomUUID() + ext;
            Path destino = dir.resolve(nombre);
            Files.copy(file.getInputStream(), destino);
            return subcarpeta + "/" + nombre;
        } catch (IOException e) {
            throw new OperacionNoPermitidaException("No se pudo guardar el archivo: " + e.getMessage());
        }
    }

    private static void validarTipo(MultipartFile file, Set<String> permitidos, String mensaje) {
        String contentType = file.getContentType();
        if (contentType == null || !permitidos.contains(contentType)) {
            throw new OperacionNoPermitidaException(mensaje);
        }
    }

    private static String extensionDesdeNombre(String original) {
        if (original == null || !original.contains(".")) {
            return "";
        }
        return original.substring(original.lastIndexOf('.'));
    }
}
