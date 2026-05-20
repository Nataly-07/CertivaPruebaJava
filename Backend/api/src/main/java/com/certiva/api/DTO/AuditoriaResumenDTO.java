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
public class AuditoriaResumenDTO {

    private Long idAuditoria;

    private String accion;

    private String entidadAfectada;

    private String descripcion;

    private String ip;

    private LocalDateTime fecha;

    private Long idUsuario;
}
