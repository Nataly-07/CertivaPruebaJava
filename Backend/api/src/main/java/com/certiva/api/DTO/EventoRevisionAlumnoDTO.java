package com.certiva.api.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoRevisionAlumnoDTO {

    private Long idInscripcion;
    private String nombres;
    private String apellidos;
    private String correo;
    private String estadoInscripcion;
    private Double nota;
    private Integer porcentajeAsistencia;
    private boolean asistenciaConfirmada;
    private boolean elegibleCertificado;
    private String motivoNoElegible;
}
