package com.certiva.api.DTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.certiva.api.enums.EstadoOperativoEvento;
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
public class EventoDTO {

    private Long idEvento;

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

    @Size(max = 500)
    private String ubicacion;

    @Size(max = 500)
    private String enlaceVirtual;

    @NotNull
    @PositiveOrZero
    private Integer aforoMaximo;

    @NotNull
    @Positive
    private Integer intensidadHoraria;

    @NotNull
    @PositiveOrZero
    private Double precio;

    private Boolean gratuito;

    /** Token para QR de difusión (inscripción pública). */
    private String codigoDifusion;

    /** URL del frontend para inscripción vía QR de difusión. */
    private String urlInscripcionPublica;

    private String rutaImagenPromocional;

    private String rutaPensum;

    @Size(max = 4000)
    private String textoDiploma;

    @Size(max = 500)
    private String firmaDigitalProfesor;

    private Boolean estado;

    private EstadoOperativoEvento estadoOperativo;

    private Long idUsuarioCreador;

    private List<Long> idsProfesoresColaboradores = new ArrayList<>();

    private List<Long> idsMonitoresAsignados = new ArrayList<>();

    private List<UsuarioStaffDTO> profesoresColaboradores = new ArrayList<>();

    private List<UsuarioStaffDTO> monitoresAsignados = new ArrayList<>();

    @Valid
    private DetalleCursoDTO detalleCurso;

    @Valid
    private DetalleHackathonDTO detalleHackathon;

    @Valid
    private DetalleFeriaDTO detalleFeria;

    @Valid
    private DetalleTallerDTO detalleTaller;

    @Valid
    private List<CampoFormularioDTO> camposPersonalizados = new ArrayList<>();
}
