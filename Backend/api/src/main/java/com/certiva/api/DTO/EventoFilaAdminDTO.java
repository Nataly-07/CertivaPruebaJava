package com.certiva.api.DTO;

import java.time.LocalDateTime;

import com.certiva.api.enums.ModalidadEvento;
import com.certiva.api.enums.TipoEventoEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoFilaAdminDTO {

    private Long idEvento;
    private String nombreEvento;
    private TipoEventoEnum tipoEvento;
    private ModalidadEvento modalidad;
    private String instructorPrincipal;
    private long inscritosActivos;
    private Integer aforoMaximo;
    private Boolean estado;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
}
