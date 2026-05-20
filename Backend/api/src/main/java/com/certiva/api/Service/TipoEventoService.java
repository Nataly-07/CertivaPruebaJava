package com.certiva.api.Service;

import java.util.List;

import com.certiva.api.DTO.CrearTipoEventoDTO;
import com.certiva.api.DTO.TipoEventoDTO;

public interface TipoEventoService {

    CrearTipoEventoDTO crearTipoEvento(CrearTipoEventoDTO tipoEventoDTO);

    List<TipoEventoDTO> listarTipoEventos();

    TipoEventoDTO actualizarTipoEvento(TipoEventoDTO tipoEventoDTO);

    String inactivarTipoEvento(Long idTipoEvento);

    boolean borrarTipoEvento(Long idTipoEvento);
}
