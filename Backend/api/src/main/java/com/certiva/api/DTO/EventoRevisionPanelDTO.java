package com.certiva.api.DTO;

import java.util.ArrayList;
import java.util.List;

import com.certiva.api.enums.EstadoOperativoEvento;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoRevisionPanelDTO {

    private Long idEvento;
    private String nombreEvento;
    private EstadoOperativoEvento estadoOperativo;
    private long totalInscritos;
    private long asistenciasRegistradas;

    @Builder.Default
    private List<EventoRevisionAlumnoDTO> alumnos = new ArrayList<>();
}
