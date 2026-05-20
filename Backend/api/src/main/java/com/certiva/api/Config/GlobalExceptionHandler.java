package com.certiva.api.Config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.HibernateException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import jakarta.persistence.PersistenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.certiva.api.DTO.ErrorRespuestaDTO;
import com.certiva.api.Exception.ConflictoOperacionException;
import com.certiva.api.Exception.CredencialesInvalidasException;
import com.certiva.api.Exception.OperacionNoPermitidaException;
import com.certiva.api.Exception.RecursoDuplicadoException;
import com.certiva.api.Exception.RecursoNoEncontradoException;
import com.certiva.api.Exception.UsuarioInactivoException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorRespuestaDTO> handleRecursoNoEncontrado(RecursoNoEncontradoException ex) {
        ErrorRespuestaDTO error = new ErrorRespuestaDTO(true, 404, ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorRespuestaDTO> handleCredencialesInvalidas(CredencialesInvalidasException ex) {
        ErrorRespuestaDTO error = new ErrorRespuestaDTO(true, 401, ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UsuarioInactivoException.class)
    public ResponseEntity<ErrorRespuestaDTO> handleUsuarioInactivo(UsuarioInactivoException ex) {
        ErrorRespuestaDTO error = new ErrorRespuestaDTO(true, 401, ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<ErrorRespuestaDTO> handleRecursoDuplicado(RecursoDuplicadoException ex) {
        ErrorRespuestaDTO error = new ErrorRespuestaDTO(true, 400, ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(OperacionNoPermitidaException.class)
    public ResponseEntity<ErrorRespuestaDTO> handleOperacionNoPermitida(OperacionNoPermitidaException ex) {
        ErrorRespuestaDTO error = new ErrorRespuestaDTO(true, 403, ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ConflictoOperacionException.class)
    public ResponseEntity<ErrorRespuestaDTO> handleConflictoOperacion(ConflictoOperacionException ex) {
        ErrorRespuestaDTO error = new ErrorRespuestaDTO(true, 409, ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuestaDTO> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> detalles = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .toList();

        ErrorRespuestaDTO error = new ErrorRespuestaDTO(true, 400, "Revise los datos enviados.", detalles);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuestaDTO> handleJsonInvalido(HttpMessageNotReadableException ex) {
        log.warn("JSON o multipart inválido: {}", ex.getMessage());
        ErrorRespuestaDTO error = new ErrorRespuestaDTO(
                true,
                400,
                "Formato de datos inválido. Verifique fechas, tipos de evento y campos numéricos.");
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HibernateException.class)
    public ResponseEntity<ErrorRespuestaDTO> handleHibernate(HibernateException ex) {
        log.error("Error de persistencia Hibernate", ex);
        ErrorRespuestaDTO error = new ErrorRespuestaDTO(
                true,
                500,
                "Error al acceder a los datos del evento. Reinicie el backend tras la migración o contacte al administrador.");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorRespuestaDTO> handleIntegridad(DataIntegrityViolationException ex) {
        log.error("Violación de integridad en BD", ex);
        String mensaje = interpretarViolacionIntegridad(ex);
        ErrorRespuestaDTO error = new ErrorRespuestaDTO(true, 409, mensaje);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorRespuestaDTO> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorRespuestaDTO error = new ErrorRespuestaDTO(true, 400, ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PersistenceException.class)
    public ResponseEntity<ErrorRespuestaDTO> handlePersistence(PersistenceException ex) {
        log.error("Error JPA al consultar datos", ex);
        ErrorRespuestaDTO error = new ErrorRespuestaDTO(
                true,
                500,
                "Error al leer eventos en base de datos. Reinicie el backend tras guardar un evento completo.");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(HttpMessageNotWritableException.class)
    public ResponseEntity<ErrorRespuestaDTO> handleNotWritable(HttpMessageNotWritableException ex) {
        log.error("Error serializando respuesta JSON", ex);
        ErrorRespuestaDTO error = new ErrorRespuestaDTO(true, 500, "Error al generar la respuesta del catálogo.");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorRespuestaDTO> handleDataAccess(DataAccessException ex) {
        log.error("Error SQL al consultar datos", ex);
        String detalle = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        String mensaje = "Error al consultar la base de datos."
                + (detalle != null && !detalle.isBlank() ? " Detalle: " + detalle : "");
        ErrorRespuestaDTO error = new ErrorRespuestaDTO(true, 500, mensaje);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorRespuestaDTO> handleIllegalState(IllegalStateException ex) {
        log.error("Estado ilegal / consulta SQL de listados", ex);
        String mensaje = ex.getMessage() != null ? ex.getMessage() : "Error al consultar eventos en la base de datos.";
        ErrorRespuestaDTO error = new ErrorRespuestaDTO(true, 500, mensaje);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuestaDTO> handleGenericException(Exception ex) {
        log.error("Error interno no controlado", ex);
        ErrorRespuestaDTO error = new ErrorRespuestaDTO(true, 500, "Error interno del servidor");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String interpretarViolacionIntegridad(DataIntegrityViolationException ex) {
        Throwable causa = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause() : ex;
        String raw = causa.getMessage() != null ? causa.getMessage().toLowerCase() : "";

        if (raw.contains("codigo_difusion") || raw.contains("codigo_difusion")) {
            return "Conflicto con el código de difusión (valor duplicado). Pulse Guardar de nuevo.";
        }
        if (raw.contains("evento_profesores") || raw.contains("evento_monitores")) {
            return "Conflicto al asignar profesores o monitores. Revise que no haya usuarios duplicados en las listas.";
        }
        if (raw.contains("foreign key") || raw.contains("violates foreign key")) {
            return "Un profesor o monitor enviado no existe en el sistema, o la referencia no es válida.";
        }
        if (raw.contains("unique") || raw.contains("duplicate key") || raw.contains("duplicada")) {
            if (raw.contains("titulo")) {
                return "Ya existe un evento con restricción única en el título. Pruebe con otro nombre.";
            }
            return "Hay un valor duplicado que viola una restricción única en la base de datos.";
        }
        if (raw.contains("not-null") || raw.contains("not null")) {
            String columna = extraerColumnaNotNull(causa.getMessage());
            if (columna != null) {
                return "Falta un dato obligatorio en la base de datos: " + etiquetaColumnaAmigable(columna)
                        + ". Revise el formulario y vuelva a guardar.";
            }
            return "Faltan datos obligatorios del evento. Revise curso, fechas, aforo e intensidad horaria.";
        }
        return "No se pudo guardar por un conflicto de datos en la base de datos. "
                + "Revise profesores/monitores, fechas y vuelva a intentar.";
    }

    private static String extraerColumnaNotNull(String mensaje) {
        if (mensaje == null) {
            return null;
        }
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("column [\"']?([\\w]+)[\"']?", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(mensaje);
        return m.find() ? m.group(1) : null;
    }

    private static String etiquetaColumnaAmigable(String columna) {
        return switch (columna.toLowerCase()) {
            case "titulo" -> "nombre del evento";
            case "cupos" -> "aforo máximo";
            case "intensidad_horaria" -> "intensidad horaria";
            case "fecha_inicio", "fechainicio" -> "fecha de inicio";
            case "fecha_fin", "fechafin" -> "fecha de fin";
            case "tipo_evento" -> "tipo de evento";
            case "id_tipo_evento" -> "tipo de evento (catálogo)";
            case "id_creador" -> "usuario creador";
            case "nivel_academico" -> "nivel académico del curso";
            case "nota_minima_aprobacion" -> "nota mínima del curso";
            case "porcentaje_asistencia_minimo" -> "asistencia mínima del curso";
            default -> columna.replace('_', ' ');
        };
    }
}
