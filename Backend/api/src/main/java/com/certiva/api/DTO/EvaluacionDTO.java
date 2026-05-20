package com.certiva.api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluacionDTO {

    private Long idEvaluacion;

    private String titulo;

    private Double puntajeAprobacion;

    private Long idEvento;
}
