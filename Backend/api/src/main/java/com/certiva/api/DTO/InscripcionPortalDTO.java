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
public class InscripcionPortalDTO {

    private Long idInscripcion;
    private String estado;
    private String tokenQr;
    private LocalDateTime fechaInscripcion;

    private Long idEvento;
    private String nombreEvento;
    private String tipoEvento;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    /** INSCRITO | EN_CURSO | FINALIZADO */
    private String fase;

    private boolean puedeDescargarCertificado;
    private Long idCertificado;
    private String motivoCertificadoPendiente;
}
