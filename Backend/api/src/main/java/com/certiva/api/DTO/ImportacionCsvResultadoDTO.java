package com.certiva.api.DTO;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportacionCsvResultadoDTO {

    private int filasExitosas;

    private int filasConError;

    @Builder.Default
    private List<String> erroresPorFila = new ArrayList<>();
}
