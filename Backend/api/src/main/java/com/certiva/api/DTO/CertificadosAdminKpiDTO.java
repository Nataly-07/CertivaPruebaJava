package com.certiva.api.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificadosAdminKpiDTO {

    private long totalEmitidos;
    private long eventosClausurados;
    private double tasaAprobacion;
    private String ultimaEmisionTexto;
    private String ultimoEventoNombre;
}
