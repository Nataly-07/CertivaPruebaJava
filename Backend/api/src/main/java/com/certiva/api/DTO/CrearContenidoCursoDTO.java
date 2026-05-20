package com.certiva.api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearContenidoCursoDTO {

    private String titulo;

    private String descripcion;

    private String urlContenido;

    private Integer orden;

    private Long idEvento;
}
