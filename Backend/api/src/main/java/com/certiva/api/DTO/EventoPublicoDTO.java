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
    private Integer porcentajeAsistenciaMinimo;
    private Double precio;
    private Boolean gratuito;
    private String rutaImagenPromocional;
    private String codigoDifusion;
    private String urlInscripcionPublica;
    private boolean hayCupoDisponible;
    private List<CampoFormularioDTO> camposPersonalizados;

    /** Etiqueta de área para badges (catálogo / detalle). */
    private String area;
    private String instructorNombres;
    private String instructorApellidos;
    private String instructorRolEtiqueta;
    private Long inscritosActivos;
    /** true si el evento admite inscripción en este momento. */
    private Boolean puedeInscribirse;
}
