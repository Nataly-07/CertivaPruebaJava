package com.certiva.api.DTO;

import com.certiva.api.enums.TipoEventoEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoResumenTipoDTO {

    private TipoEventoEnum tipo;
    private long totalEventos;
    private double porcentajeOcupacion;
}
