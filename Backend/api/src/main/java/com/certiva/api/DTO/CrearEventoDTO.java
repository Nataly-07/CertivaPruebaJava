package com.certiva.api.DTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.certiva.api.enums.ModalidadEvento;
import com.certiva.api.enums.TipoEventoEnum;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearEventoDTO {

    @NotBlank
    @Size(max = 150)
    private String nombreEvento;

    @Size(max = 8000)
    private String descripcion;

    @NotNull
    private TipoEventoEnum tipoEvento;

    @NotNull
    private ModalidadEvento modalidad;

    @NotNull
    private LocalDateTime fechaInicio;

    @NotNull
    private LocalDateTime fechaFin;

    @Size(max = 2048)
    private String ubicacion;

    @Size(max = 2048)
    private String enlaceVirtual;

    @NotNull
    @PositiveOrZero
    private Integer aforoMaximo;

    @NotNull
    @Positive
    private Integer intensidadHoraria;

    /** Asistencia mínima (%) requerida para certificar (1–100). Obligatorio en todos los tipos. */
    @NotNull
    private Integer porcentajeAsistenciaMinimo;

    @NotNull
    @PositiveOrZero
    private Double precio;

    @Size(max = 4000)
    private String textoDiploma;

    @Size(max = 500)
    private String firmaDigitalProfesor;

    @NotNull
    private Long idProfesorLider;

    @Size(max = 10000)
    private String imagenPromocionalUrl;

    private List<Long> idsProfesoresColaboradores = new ArrayList<>();

    private List<Long> idsMonitoresAsignados = new ArrayList<>();

    @Valid
    private DetalleCursoDTO detalleCurso;

    @Valid
    private DetalleHackathonDTO detalleHackathon;

    @Valid
    private DetalleFeriaDTO detalleFeria;

    @Valid
    private DetalleTallerDTO detalleTaller;

    @Valid
    private List<CrearCampoFormularioDTO> camposPersonalizados = new ArrayList<>();
}
