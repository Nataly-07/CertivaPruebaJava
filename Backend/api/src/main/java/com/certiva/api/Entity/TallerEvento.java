package com.certiva.api.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "taller_evento")
@PrimaryKeyJoinColumn(name = "id_evento")
public class TallerEvento extends Evento {

    @Column(name = "material_guia", columnDefinition = "TEXT")
    private String materialGuia;
}
