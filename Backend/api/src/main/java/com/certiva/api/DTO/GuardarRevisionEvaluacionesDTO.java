package com.certiva.api.DTO;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuardarRevisionEvaluacionesDTO {

    @Builder.Default
    private List<GuardarRevisionAlumnoDTO> alumnos = new ArrayList<>();
}
