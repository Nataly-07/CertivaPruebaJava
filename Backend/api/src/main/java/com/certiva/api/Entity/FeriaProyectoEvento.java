package com.certiva.api.Entity;

import com.certiva.api.enums.CategoriaExhibicionFeria;

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
@Table(name = "feria_proyecto_evento")
@PrimaryKeyJoinColumn(name = "id_evento")
public class FeriaProyectoEvento extends Evento {

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_exhibicion", length = 40)
    private CategoriaExhibicionFeria categoriaExhibicion;

    /** JSON array de tecnologías permitidas. */
    @Column(name = "stack_tecnologico", columnDefinition = "TEXT")
    private String stackTecnologico;

    @Column(name = "criterios_evaluacion", columnDefinition = "TEXT")
    private String criteriosEvaluacion;
}
