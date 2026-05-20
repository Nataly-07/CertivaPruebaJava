package com.certiva.api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearResultadoEventoDTO {

    private Double puntaje;

    private Long idEvento;

    private Long idUsuario;
}
