package com.certiva.api.DTO;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorRespuestaDTO {

    private boolean error;

    private int codigo;

    private String mensaje;

    private LocalDateTime fecha;

    private List<String> detalles;

    public ErrorRespuestaDTO(boolean error, int codigo, String mensaje) {
        this.error = error;
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.fecha = LocalDateTime.now();
    }

    public ErrorRespuestaDTO(boolean error, int codigo, String mensaje, List<String> detalles) {
        this.error = error;
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.fecha = LocalDateTime.now();
        this.detalles = detalles;
    }
}
