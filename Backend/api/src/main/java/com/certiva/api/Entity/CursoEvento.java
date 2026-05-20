package com.certiva.api.Entity;

import com.certiva.api.enums.NivelAcademico;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "curso_evento")
@PrimaryKeyJoinColumn(name = "id_evento")
public class CursoEvento extends Evento {

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_academico", length = 20)
    private NivelAcademico nivelAcademico;

    @Column(name = "nota_minima_aprobacion")
    private Double notaMinimaAprobacion;

    @Column(name = "porcentaje_asistencia_minimo")
    private Integer porcentajeAsistenciaMinimo;
}
