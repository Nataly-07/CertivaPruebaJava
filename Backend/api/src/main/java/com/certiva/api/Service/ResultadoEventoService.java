package com.certiva.api.Service;

import java.util.List;

import com.certiva.api.DTO.CrearResultadoEventoDTO;
import com.certiva.api.DTO.ResultadoEventoDTO;

public interface ResultadoEventoService {

    CrearResultadoEventoDTO crearResultadoEvento(CrearResultadoEventoDTO resultadoDTO);

    List<ResultadoEventoDTO> listarResultadoEventos();

    ResultadoEventoDTO actualizarResultadoEvento(ResultadoEventoDTO resultadoDTO);

    String inactivarResultadoEvento(Long id);

    boolean borrarResultadoEvento(Long id);
}
