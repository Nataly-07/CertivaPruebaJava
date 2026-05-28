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
public class EventoAsistenciaEnVivoDTO {

    private Long idEvento;
    private String nombreEvento;
    private EstadoOperativoEvento estadoOperativo;
    private long totalInscritos;
    private long asistenciasConfirmadas;
    private int porcentajeAsistenciaGlobal;
    private int asistenciaPromedioSesionHoy;

    @Builder.Default
    private List<ProfesorAlumnoAsistenciaDTO> alumnos = new ArrayList<>();
}
