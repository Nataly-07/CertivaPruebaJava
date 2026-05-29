package com.certiva.api.DTO;

import java.time.LocalDateTime;

import com.certiva.api.enums.EstadoOperativoEvento;
import com.certiva.api.enums.MonitorNivelAlerta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitorEventoTarjetaDTO {

    private Long idEvento;
    private String nombreEvento;
    private String tipoEvento;
    private String ubicacion;
    private EstadoOperativoEvento estadoOperativo;
    private MonitorNivelAlerta nivelAlerta;

    private long inscritosActivos;
    private long asistenciasConfirmadas;
    private int porcentajeCheckIn;

    private int sesionesTotales;
    private int sesionActual;

    /** Minutos restantes hasta {@code fechaFin}; null si no aplica. */
    private Long minutosHastaFin;

    private String profesorNombre;
    private String profesorApellidos;
    private String profesorCorreo;

    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    private boolean permiteAbrirCheckIn;
    private boolean sesionHoy;
}
