package com.certiva.api.Service;

import java.util.List;

import com.certiva.api.DTO.CrearEvaluacionDTO;
import com.certiva.api.DTO.EvaluacionDTO;

public interface EvaluacionService {

    CrearEvaluacionDTO crearEvaluacion(CrearEvaluacionDTO evaluacionDTO);

    List<EvaluacionDTO> listarEvaluaciones();

    EvaluacionDTO actualizarEvaluacion(EvaluacionDTO evaluacionDTO);

    String inactivarEvaluacion(Long idEvaluacion);

    boolean borrarEvaluacion(Long idEvaluacion);
}
