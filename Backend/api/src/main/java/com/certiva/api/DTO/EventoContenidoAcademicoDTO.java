package com.certiva.api.DTO;

import java.util.ArrayList;
import java.util.List;

import com.certiva.api.enums.TipoEventoEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoContenidoAcademicoDTO {

    private Long idEvento;
    private String nombreEvento;
    private TipoEventoEnum tipoEvento;
    private String avisosReglas;
    @Builder.Default
    private List<RecursoAcademicoDTO> recursos = new ArrayList<>();
    private String materialGuia;
    private String retoTecnicoCentral;
    private String premiosIncentivos;
    private String criteriosEvaluacion;
}
