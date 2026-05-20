package com.certiva.api.Impl;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.certiva.api.DTO.CrearContenidoCursoDTO;
import com.certiva.api.DTO.ContenidoCursoDTO;
import com.certiva.api.Entity.ContenidoCurso;
import com.certiva.api.Entity.Evento;
import com.certiva.api.Repository.ContenidoCursoRepository;
import com.certiva.api.Repository.EventoRepository;
import com.certiva.api.Service.ContenidoCursoService;

@Service
public class ContenidoCursoServiceImpl implements ContenidoCursoService {

    private final ContenidoCursoRepository _contenidoCursoRepository;
    private final EventoRepository _eventoRepository;
    private final ModelMapper _modelMapper;

    public ContenidoCursoServiceImpl(ContenidoCursoRepository contenidoCursoRepository,
                                     EventoRepository eventoRepository,
                                     ModelMapper modelMapper) {
        this._contenidoCursoRepository = contenidoCursoRepository;
        this._eventoRepository = eventoRepository;
        this._modelMapper = modelMapper;
    }

    @Override
    public CrearContenidoCursoDTO crearContenidoCurso(CrearContenidoCursoDTO contenidoDTO) {
        ContenidoCurso contenido = new ContenidoCurso();
        contenido.setTitulo(contenidoDTO.getTitulo());
        contenido.setDescripcion(contenidoDTO.getDescripcion());
        contenido.setUrlContenido(contenidoDTO.getUrlContenido());
        contenido.setOrden(contenidoDTO.getOrden());

        Evento evento = _eventoRepository.findById(contenidoDTO.getIdEvento())
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
        contenido.setEvento(evento);

        contenido = _contenidoCursoRepository.save(contenido);
        return _modelMapper.map(contenido, CrearContenidoCursoDTO.class);
    }

    @Override
    public List<ContenidoCursoDTO> listarContenidoCursos() {
        List<ContenidoCurso> listado = _contenidoCursoRepository.findAll();
        return listado.stream()
                .map(c -> {
                    ContenidoCursoDTO dto = _modelMapper.map(c, ContenidoCursoDTO.class);
                    dto.setIdEvento(c.getEvento().getIdEvento());
                    return dto;
                })
                .toList();
    }

    @Override
    public ContenidoCursoDTO actualizarContenidoCurso(ContenidoCursoDTO contenidoDTO) {
        ContenidoCurso contenido = _contenidoCursoRepository.findById(contenidoDTO.getIdContenido())
                .orElseThrow(() -> new RuntimeException("Contenido de curso no encontrado"));

        contenido.setTitulo(contenidoDTO.getTitulo());
        contenido.setDescripcion(contenidoDTO.getDescripcion());
        contenido.setUrlContenido(contenidoDTO.getUrlContenido());
        contenido.setOrden(contenidoDTO.getOrden());

        if (contenidoDTO.getIdEvento() != null) {
            Evento evento = _eventoRepository.findById(contenidoDTO.getIdEvento())
                    .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
            contenido.setEvento(evento);
        }

        contenido = _contenidoCursoRepository.save(contenido);
        ContenidoCursoDTO dto = _modelMapper.map(contenido, ContenidoCursoDTO.class);
        dto.setIdEvento(contenido.getEvento().getIdEvento());
        return dto;
    }

    @Override
    public String inactivarContenidoCurso(Long idContenido) {
        ContenidoCurso contenido = _contenidoCursoRepository.findById(idContenido)
                .orElseThrow(() -> new RuntimeException("Contenido de curso no encontrado"));
        _contenidoCursoRepository.delete(contenido);
        return "Contenido de curso inactivado correctamente";
    }

    @Override
    public boolean borrarContenidoCurso(Long idContenido) {
        ContenidoCurso contenido = _contenidoCursoRepository.findById(idContenido)
                .orElseThrow(() -> new RuntimeException("Contenido de curso no encontrado"));
        _contenidoCursoRepository.delete(contenido);
        return true;
    }
}
