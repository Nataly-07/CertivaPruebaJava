package com.certiva.api.DTO;

import java.time.LocalDateTime;
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
public class ProfesorPanelDTO {

    private int totalEventos;
    private long totalInscritos;
    private long eventosActivos;

    @Builder.Default
    private List<ProfesorEventoResumenDTO> eventos = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfesorEventoResumenDTO {
        private Long idEvento;
        private String nombreEvento;
        private String tipoEvento;
        private boolean activo;
        private long inscritos;
        private LocalDateTime fechaInicio;
        private LocalDateTime fechaFin;
    }
}
