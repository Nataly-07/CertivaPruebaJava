package com.certiva.api.DTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaDTO {

    private Long idAuditoria;

    private String accion;

    private String descripcion;

    private String ip;

    private LocalDateTime fecha;

    private Long idUsuario;
}
