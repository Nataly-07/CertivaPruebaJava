package com.certiva.api.DTO;

import com.certiva.api.enums.NivelAcademico;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DetalleCursoDTO {

    @NotNull
    private NivelAcademico nivelAcademico;

    @NotNull
    private Double notaMinimaAprobacion;

    @NotNull
    @Min(0)
    @Max(100)
    private Integer porcentajeAsistenciaMinimo;
}
