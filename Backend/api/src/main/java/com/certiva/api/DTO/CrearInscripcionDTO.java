package com.certiva.api.DTO;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearInscripcionDTO {

    @NotNull(message = "El usuario es obligatorio")
    private Long idUsuario;

    @NotNull(message = "El evento es obligatorio")
    private Long idEvento;

    /** Solo salida: token QR generado al crear la inscripción */
    private String tokenQr;

    @Valid
    private List<RespuestaCampoDTO> respuestasCampos = new ArrayList<>();
}
