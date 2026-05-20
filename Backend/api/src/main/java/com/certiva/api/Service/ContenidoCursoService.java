package com.certiva.api.Service;

import java.util.List;

import com.certiva.api.DTO.CrearContenidoCursoDTO;
import com.certiva.api.DTO.ContenidoCursoDTO;

public interface ContenidoCursoService {

    CrearContenidoCursoDTO crearContenidoCurso(CrearContenidoCursoDTO contenidoCursoDTO);

    List<ContenidoCursoDTO> listarContenidoCursos();

    ContenidoCursoDTO actualizarContenidoCurso(ContenidoCursoDTO contenidoCursoDTO);

    String inactivarContenidoCurso(Long idContenido);

    boolean borrarContenidoCurso(Long idContenido);
}
