package com.certiva.api.Entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.certiva.api.enums.EstadoOperativoEvento;
import com.certiva.api.enums.ModalidadEvento;
import com.certiva.api.enums.TipoEventoEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(exclude = { "usuarioCreador", "profesoresColaboradores", "monitoresAsignados", "catalogoTipoEvento" })
@Entity
@Table(name = "evento")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evento")
    private Long idEvento;

    @Column(name = "titulo", nullable = false, length = 150)
    private String nombreEvento;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private Double costo = 0.0;

    @Column(name = "cupos", nullable = false)
    private Integer aforoMaximo;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ModalidadEvento modalidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", length = 20, nullable = false)
    private TipoEventoEnum tipoEvento;

    /**
     * FK legada a {@code TipoEvento}. Nullable en JPA para permitir migración con datos existentes;
     * el backfill de arranque y la validación al crear eventos garantizan el valor.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "id_tipo_evento", nullable = true)
    private TipoEvento catalogoTipoEvento;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    @Column(length = 500)
    private String ubicacion;

    @Column(name = "enlace_virtual", length = 500)
    private String enlaceVirtual;

    @Column(name = "intensidad_horaria", nullable = false)
    private Integer intensidadHoraria;

    /** Porcentaje mínimo de asistencia requerido para emitir certificado (1–100). */
    @Column(name = "porcentaje_asistencia_minimo")
    private Integer porcentajeAsistenciaMinimo = 80;

    @Column(name = "imagen_promocional", columnDefinition = "TEXT")
    private String rutaImagenPromocional;

    @Column(name = "ruta_pensum", length = 500)
    private String rutaPensum;

    /** Token único para QR de difusión / inscripción pública. */
    @Column(name = "codigo_difusion", unique = true, length = 64)
    private String codigoDifusion;

    @Column(name = "texto_diploma", columnDefinition = "TEXT")
    private String textoDiploma;

    @Column(name = "firma_digital_profesor", length = 500)
    private String firmaDigitalProfesor;

    /** JSON: [{ "tipo": "ENLACE|GITHUB|DOCUMENTO", "titulo": "...", "url": "..." }] */
    @Column(name = "recursos_academicos", columnDefinition = "TEXT")
    private String recursosAcademicos;

    /** Pautas de retos, reglas del torneo o avisos para participantes. */
    @Column(name = "avisos_reglas", columnDefinition = "TEXT")
    private String avisosReglas;

    /** Publicación / catálogo (false = evento retirado). */
    private Boolean estado = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_operativo", length = 40, nullable = false)
    private EstadoOperativoEvento estadoOperativo = EstadoOperativoEvento.PROXIMO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_creador", nullable = false)
    private Usuario usuarioCreador;

    /** Staff académico (ROLE_PROFESOR). Distinto de inscripciones de estudiantes. */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "evento_profesores",
            joinColumns = @JoinColumn(name = "id_evento"),
            inverseJoinColumns = @JoinColumn(name = "id_usuario"))
    private Set<Usuario> profesoresColaboradores = new HashSet<>();

    /** Personal logístico (ROLE_MONITOR). No incluye estudiantes. */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "evento_monitores",
            joinColumns = @JoinColumn(name = "id_evento"),
            inverseJoinColumns = @JoinColumn(name = "id_usuario"))
    private Set<Usuario> monitoresAsignados = new HashSet<>();

    @PrePersist
    void prePersistDefaults() {
        if (estado == null) {
            estado = true;
        }
        if (estadoOperativo == null) {
            estadoOperativo = EstadoOperativoEvento.PROXIMO;
        }
        if (costo == null) {
            costo = 0.0;
        }
        if (modalidad == null) {
            modalidad = ModalidadEvento.PRESENCIAL;
        }
        if (codigoDifusion == null || codigoDifusion.isBlank()) {
            codigoDifusion = UUID.randomUUID().toString().replace("-", "");
        }
        if (porcentajeAsistenciaMinimo == null) {
            porcentajeAsistenciaMinimo = 80;
        }
    }

    public boolean isGratuito() {
        return costo == null || costo <= 0.0;
    }
}
