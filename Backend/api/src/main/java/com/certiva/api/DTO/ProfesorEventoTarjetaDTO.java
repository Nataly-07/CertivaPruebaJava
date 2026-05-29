package com.certiva.api.DTO;

import java.time.LocalDateTime;

import com.certiva.api.enums.EstadoOperativoEvento;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfesorEventoTarjetaDTO {

    private Long idEvento;
    private String nombreEvento;
    private String tipoEvento;
    private EstadoOperativoEvento estadoOperativo;
    private long inscritosActivos;
    private long asistenciasConfirmadas;
    private int porcentajeAsistenciaGlobal;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String monitorNombre;
    private String monitorApellidos;
    /** FINALIZADO_POR_TIEMPO requiere iniciar revisión antes del cierre. */
    private boolean requiereIniciarRevision;

    /** Total de sesiones (Y en «Clase X de Y»). */
    private int sesionesTotales;

    /** Sesión actual del ciclo (X). */
    private int sesionActual;

    /** Asistencia mínima (%) configurada en el evento. */
    private int porcentajeAsistenciaMinimo;

    /** Listo para clausura: sesiones completas y estado EN_REVISION. */
    private boolean listoParaClausurar;
}
