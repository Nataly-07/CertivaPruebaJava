package com.certiva.api.DTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InscripcionDTO {

    private Long idInscripcion;

    private String estado;

    private Boolean pagoRealizado;

    private LocalDateTime fechaInscripcion;

    private Long idUsuario;

    private Long idEvento;

    private String tokenQr;

    private List<RespuestaCampoDTO> respuestasCampos = new ArrayList<>();
}
