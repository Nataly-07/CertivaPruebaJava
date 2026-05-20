package com.certiva.api.DTO;

import com.certiva.api.enums.TipoDatoCampo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearCampoFormularioDTO {

    @NotBlank
    @Size(max = 255)
    private String etiqueta;

    @NotNull
    private TipoDatoCampo tipoDato;

    private boolean esObligatorio;

    /** JSON array de strings; obligatorio si tipoDato es SELECT. */
    @Size(max = 8000)
    private String opciones;
}
