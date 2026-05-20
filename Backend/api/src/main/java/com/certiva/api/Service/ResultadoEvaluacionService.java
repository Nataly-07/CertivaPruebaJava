package com.certiva.api.Service;

import java.util.List;

import com.certiva.api.DTO.CrearResultadoEvaluacionDTO;
import com.certiva.api.DTO.ResultadoEvaluacionDTO;

public interface ResultadoEvaluacionService {

    CrearResultadoEvaluacionDTO crearResultadoEvaluacion(CrearResultadoEvaluacionDTO resultadoDTO);

    List<ResultadoEvaluacionDTO> listarResultadoEvaluaciones();

    ResultadoEvaluacionDTO actualizarResultadoEvaluacion(ResultadoEvaluacionDTO resultadoDTO);

    String inactivarResultadoEvaluacion(Long id);

    boolean borrarResultadoEvaluacion(Long id);
}
