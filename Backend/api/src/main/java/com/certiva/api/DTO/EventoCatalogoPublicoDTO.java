package com.certiva.api.DTO;

import java.time.LocalDateTime;

import com.certiva.api.enums.ModalidadEvento;
import com.certiva.api.enums.TipoEventoEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Evento activo expuesto en el catálogo del portal estudiante (sin herencia JPA). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoCatalogoPublicoDTO {

    private Long idEvento;
    private String nombreEvento;
    private String descripcion;
    private TipoEventoEnum tipoEvento;
    /** Etiqueta de área para badges (ej. TECNOLOGÍA, DESARROLLO). */
    private String area;
    private ModalidadEvento modalidad;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String ubicacion;
    private String enlaceVirtual;
    private Integer aforoMaximo;
    private Double precio;
    private Boolean gratuito;
    private String rutaImagenPromocional;
    private String instructorNombres;
    private String instructorApellidos;
    private String instructorRolEtiqueta;
    private long inscritosActivos;
    private boolean hayCupoDisponible;
}
