package com.certiva.api.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoCierreResultadoDTO {
    private Long idEvento;
    private String estadoOperativo;
    private int certificadosEmitidos;
    private int inscripcionesPendientesCertificado;
    private String mensaje;
}
