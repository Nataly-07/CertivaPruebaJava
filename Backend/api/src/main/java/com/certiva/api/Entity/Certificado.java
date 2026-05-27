package com.certiva.api.Entity;

import java.time.LocalDateTime;

import com.certiva.api.enums.EstadoCertificado;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "Certificado")
public class Certificado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_certificado")
    private Long idCertificado;

    @Column(nullable = false)
    private String tipoCertificado;

    @Column(nullable = false, unique = true)
    private String codigoValidacion;

    @Column(name = "fecha_emision")
    private LocalDateTime fechaEmision;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    private EstadoCertificado estado = EstadoCertificado.VALIDO;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "contenido_pdf")
    private byte[] contenidoPdf;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_evento", nullable = false)
    private Evento evento;

    @PrePersist
    void prePersist() {
        if (estado == null) {
            estado = EstadoCertificado.VALIDO;
        }
    }
}
