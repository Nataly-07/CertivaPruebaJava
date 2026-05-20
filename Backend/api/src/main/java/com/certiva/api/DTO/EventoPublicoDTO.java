package com.certiva.api.DTO;

import java.time.LocalDateTime;
import java.util.List;

import com.certiva.api.enums.ModalidadEvento;
import com.certiva.api.enums.TipoEventoEnum;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventoPublicoDTO {
    private Long idEvento;
    private String nombreEvento;
    private String descripcion;
    private TipoEventoEnum tipoEvento;
    private ModalidadEvento modalidad;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String ubicacion;
    private String enlaceVirtual;
    private Integer aforoMaximo;
    private Integer intensidadHoraria;
    private Double precio;
    private Boolean gratuito;
    private String rutaImagenPromocional;
    private String codigoDifusion;
    private String urlInscripcionPublica;
    private boolean hayCupoDisponible;
    private List<CampoFormularioDTO> camposPersonalizados;
}
