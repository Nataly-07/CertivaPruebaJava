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
public class CertificadoVerificacionDTO {

    private boolean valido;

    private String codigoValidacion;

    private String nombreParticipante;

    private String tituloEvento;

    private LocalDateTime fechaEmision;

    private String mensaje;
}
