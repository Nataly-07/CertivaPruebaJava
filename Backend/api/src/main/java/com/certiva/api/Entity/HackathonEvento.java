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
@Table(name = "hackathon_evento")
@PrimaryKeyJoinColumn(name = "id_evento")
public class HackathonEvento extends Evento {

    @Column(name = "reto_tecnico", columnDefinition = "TEXT")
    private String retoTecnicoCentral;

    @Column(name = "min_integrantes")
    private Integer minIntegrantes;

    @Column(name = "max_integrantes")
    private Integer maxIntegrantes;

    @Column(name = "premios_incentivos", columnDefinition = "TEXT")
    private String premiosIncentivos;
}
