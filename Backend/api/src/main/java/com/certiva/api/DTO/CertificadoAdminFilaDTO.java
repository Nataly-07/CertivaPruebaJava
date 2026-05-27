package com.certiva.api.DTO;

import java.time.LocalDateTime;

import com.certiva.api.enums.EstadoCertificado;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificadoAdminFilaDTO {

    private Long idCertificado;
    private String codigoValidacion;
    private String codigoMostrar;
    private String nombreParticipante;
    private String numeroDocumento;
    private Long idEvento;
    private String nombreEvento;
    private LocalDateTime fechaEmision;
    private EstadoCertificado estado;
    private boolean tienePdf;
}
