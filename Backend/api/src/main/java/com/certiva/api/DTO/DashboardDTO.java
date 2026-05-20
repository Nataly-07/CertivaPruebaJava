package com.certiva.api.DTO;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {

    private Long totalUsuarios;

    private Long eventosActivos;

    private Long asistenciasTotales;

    private Long certificadosEmitidos;

    @Builder.Default
    private Map<String, Long> distribucionRoles = new HashMap<>();
}
