package com.certiva.api.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearCertificadoDTO {

    @NotBlank(message = "El tipo de certificado es obligatorio")
    private String tipoCertificado;

    @NotNull(message = "El usuario es obligatorio")
    private Long idUsuario;

    @NotNull(message = "El evento es obligatorio")
    private Long idEvento;

    /** Respuesta: código único de verificación pública */
    private String codigoValidacion;
}
