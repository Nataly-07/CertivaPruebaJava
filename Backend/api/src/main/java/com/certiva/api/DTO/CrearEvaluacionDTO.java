package com.certiva.api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearEvaluacionDTO {

    private String titulo;

    private Double puntajeAprobacion;

    private Long idEvento;
}
