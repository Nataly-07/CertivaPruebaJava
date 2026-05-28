package com.certiva.api.DTO;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfesorParticipanteDTO {

    private Long idInscripcion;
    private String nombres;
    private String apellidos;
    private String correo;
    private String numeroDocumento;
    private String estadoInscripcion;
    /** Resumen legible del perfil técnico (respuestas del formulario de inscripción). */
    private String perfilTecnico;
    @Builder.Default
    private List<RespuestaCampoDTO> respuestasFormulario = new ArrayList<>();
}
