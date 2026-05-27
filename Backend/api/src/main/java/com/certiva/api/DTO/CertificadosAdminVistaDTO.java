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
public class CertificadosAdminVistaDTO {

    private CertificadosAdminKpiDTO kpis;

    @Builder.Default
    private List<CertificadoAdminFilaDTO> certificados = new ArrayList<>();

    /** Opciones para el filtro por evento (id + nombre). */
    @Builder.Default
    private List<EventoOpcionFiltroDTO> eventosFiltro = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventoOpcionFiltroDTO {
        private Long idEvento;
        private String nombreEvento;
    }
}
