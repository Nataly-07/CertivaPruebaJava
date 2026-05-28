package com.certiva.api.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecursoAcademicoDTO {

    /** ENLACE, GITHUB, DOCUMENTO */
    private String tipo;
    private String titulo;
    private String url;
}
