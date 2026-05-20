package com.certiva.api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoEventoDTO {

    private Long id;

    private Double puntaje;

    private Integer posicion;

    private Boolean esGanador;

    private Long idEvento;

    private Long idUsuario;
}
