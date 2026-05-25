package com.certiva.api.DTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfesorPanelBannerDTO {

    private boolean sesionActivaHoy;
    private Long idEvento;
    private String nombreEvento;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String monitorNombre;
    private String monitorApellidos;
}
