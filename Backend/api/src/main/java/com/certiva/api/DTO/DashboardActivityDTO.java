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
public class DashboardActivityDTO {

    private Integer rangoDias;

    @Builder.Default
    private List<DashboardActivityPointDTO> puntos = new ArrayList<>();

    /**
     * Últimos registros de auditoría HTTP / sistema para línea de tiempo del panel.
     */
    @Builder.Default
    private List<AuditoriaResumenDTO> auditoriaReciente = new ArrayList<>();
}
