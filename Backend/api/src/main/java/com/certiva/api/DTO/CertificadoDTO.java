package com.certiva.api.DTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificadoDTO {

    private Long idCertificado;

    private String tipoCertificado;

    private String codigoValidacion;

    private LocalDateTime fechaEmision;

    private Long idUsuario;

    private Long idEvento;
}
