package com.certiva.api.DTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificadoPortalDTO {

    private Long idCertificado;
    private String codigoValidacion;
    private String nombreEvento;
    private String tipoEvento;
    private LocalDateTime fechaEmision;
    private boolean puedeDescargar;
    private String motivoPendiente;
}
