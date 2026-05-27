package com.certiva.api.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuardarRevisionAlumnoDTO {

    private Long idInscripcion;
    private Double nota;
    private String observaciones;
}
