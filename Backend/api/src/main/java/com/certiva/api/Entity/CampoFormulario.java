package com.certiva.api.Entity;

import com.certiva.api.enums.TipoDatoCampo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "CampoFormulario")
public class CampoFormulario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_campo")
    private Long idCampo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_evento", nullable = false)
    private Evento evento;

    @Column(nullable = false, length = 255)
    private String etiqueta;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_dato", nullable = false, length = 20)
    private TipoDatoCampo tipoDato;

    @Column(name = "es_obligatorio", nullable = false)
    private boolean esObligatorio;

    /** JSON array de strings para {@link TipoDatoCampo#SELECT}; nulo en otros tipos. */
    @Column(columnDefinition = "TEXT")
    private String opciones;
}
