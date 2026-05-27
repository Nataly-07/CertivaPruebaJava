package com.certiva.api.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfesorAlumnoAsistenciaDTO {

    private Long idInscripcion;
    private String nombres;
    private String apellidos;
    private String correo;
    private String numeroDocumento;
    private String estadoInscripcion;
    private boolean asistenciaConfirmada;
    private int porcentajeAsistencia;
    private String tokenQr;
}
