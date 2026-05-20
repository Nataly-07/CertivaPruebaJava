package com.certiva.api.DTO;

import com.certiva.api.enums.CategoriaExhibicionFeria;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DetalleFeriaDTO {

    @NotNull
    private CategoriaExhibicionFeria categoriaExhibicion;

    /** JSON array de strings con tecnologías. */
    @NotBlank
    private String stackTecnologico;

    @NotBlank
    private String criteriosEvaluacion;
}
