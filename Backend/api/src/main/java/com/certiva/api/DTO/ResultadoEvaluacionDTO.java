package com.certiva.api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoEvaluacionDTO {

    private Long id;

    private Double nota;

    private Boolean aprobado;

    private Long idInscripcion;
}
