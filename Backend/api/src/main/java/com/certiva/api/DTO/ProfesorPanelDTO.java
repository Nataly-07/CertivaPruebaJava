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
public class ProfesorPanelDTO {

    private int totalEventos;
    private long totalInscritos;
    private long eventosActivos;
    /** Cursos en EN_REVISION o FINALIZADO_POR_TIEMPO pendientes de acta. */
    private long eventosPorCertificar;
    private long accionesPendientes;

    private ProfesorPanelBannerDTO banner;

    @Builder.Default
    private List<ProfesorEventoTarjetaDTO> enCurso = new ArrayList<>();

    @Builder.Default
    private List<ProfesorEventoTarjetaDTO> pendientesCierre = new ArrayList<>();

    @Builder.Default
    private List<ProfesorEventoTarjetaDTO> historial = new ArrayList<>();

    /** Compatibilidad: listado plano legacy. */
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
        private com.certiva.api.enums.EstadoOperativoEvento estadoOperativo;
        private long inscritos;
        private java.time.LocalDateTime fechaInicio;
        private java.time.LocalDateTime fechaFin;
    }
}
