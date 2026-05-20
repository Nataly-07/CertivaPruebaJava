package com.certiva.api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearTipoEventoDTO {

    private String nombre;

    private String descripcion;

    private Boolean tieneEvaluacion;

    private Boolean tieneGanador;
}
