package com.certiva.api.DTO;

import com.certiva.api.enums.TipoDatoCampo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampoFormularioDTO {

    private Long idCampo;
    private Long idEvento;
    private String etiqueta;
    private TipoDatoCampo tipoDato;
    private boolean esObligatorio;
    /** JSON array de strings (opciones de SELECT). */
    private String opciones;
}
