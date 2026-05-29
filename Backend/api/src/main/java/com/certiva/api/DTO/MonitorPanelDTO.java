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
public class MonitorPanelDTO {

    private int eventosHoy;
    private int salonesOcupados;
    private int salonesTotales;

    private long checkInConfirmados;
    private long checkInEsperados;

    private int alertasCriticas;
    private int alertasAdvertencia;

    @Builder.Default
    private List<MonitorEventoTarjetaDTO> eventos = new ArrayList<>();
}
