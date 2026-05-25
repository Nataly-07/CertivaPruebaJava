package com.certiva.api.Service;

import java.util.List;

import com.certiva.api.DTO.CheckInRespuestaDTO;
import com.certiva.api.DTO.CrearInscripcionDTO;
import com.certiva.api.DTO.InscripcionDTO;
import com.certiva.api.DTO.InscripcionPortalDTO;

public interface InscripcionService {

    CrearInscripcionDTO crearInscripcion(CrearInscripcionDTO inscripcionDTO);

    List<InscripcionDTO> listarInscripciones();

    InscripcionDTO actualizarInscripcion(InscripcionDTO inscripcionDTO);

    String inactivarInscripcion(Long idInscripcion);

    boolean borrarInscripcion(Long idInscripcion);

    CheckInRespuestaDTO confirmarAsistenciaPorCodigoQr(String codigo, String tipoAsistencia);

    String cancelarMiInscripcion(Long idInscripcion);

    List<InscripcionPortalDTO> listarMisInscripciones();
}
