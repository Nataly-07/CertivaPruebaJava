package com.certiva.api.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInRespuestaDTO {

    private String mensaje;

    private Long idInscripcion;

    private String estadoInscripcion;

    private String codigoCertificado;

    private Long idCertificado;

    /** Motivo por el cual no se emitió certificado automático tras el check-in (p. ej. falta de nota en curso). */
    private String certificadoPendienteMotivo;
}
