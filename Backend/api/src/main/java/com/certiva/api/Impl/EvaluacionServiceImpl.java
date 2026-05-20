package com.certiva.api.Impl;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.certiva.api.DTO.CrearEvaluacionDTO;
import com.certiva.api.DTO.EvaluacionDTO;
import com.certiva.api.Entity.Evaluacion;
import com.certiva.api.Entity.Evento;
import com.certiva.api.Repository.EvaluacionRepository;
import com.certiva.api.Repository.EventoRepository;
import com.certiva.api.Service.EvaluacionService;

@Service
public class EvaluacionServiceImpl implements EvaluacionService {

    private final EvaluacionRepository _evaluacionRepository;
    private final EventoRepository _eventoRepository;
    private final ModelMapper _modelMapper;

    public EvaluacionServiceImpl(EvaluacionRepository evaluacionRepository,
                                 EventoRepository eventoRepository,
                                 ModelMapper modelMapper) {
        this._evaluacionRepository = evaluacionRepository;
        this._eventoRepository = eventoRepository;
        this._modelMapper = modelMapper;
    }

    @Override
    public CrearEvaluacionDTO crearEvaluacion(CrearEvaluacionDTO evaluacionDTO) {
        Evaluacion evaluacion = new Evaluacion();
        evaluacion.setTitulo(evaluacionDTO.getTitulo());
        evaluacion.setPuntajeAprobacion(evaluacionDTO.getPuntajeAprobacion());

        Evento evento = _eventoRepository.findById(evaluacionDTO.getIdEvento())
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
        evaluacion.setEvento(evento);

        evaluacion = _evaluacionRepository.save(evaluacion);
        return _modelMapper.map(evaluacion, CrearEvaluacionDTO.class);
    }

    @Override
    public List<EvaluacionDTO> listarEvaluaciones() {
        List<Evaluacion> listado = _evaluacionRepository.findAll();
        return listado.stream()
                .map(e -> {
                    EvaluacionDTO dto = _modelMapper.map(e, EvaluacionDTO.class);
                    dto.setIdEvento(e.getEvento().getIdEvento());
                    return dto;
                })
                .toList();
    }

    @Override
    public EvaluacionDTO actualizarEvaluacion(EvaluacionDTO evaluacionDTO) {
        Evaluacion evaluacion = _evaluacionRepository.findById(evaluacionDTO.getIdEvaluacion())
                .orElseThrow(() -> new RuntimeException("Evaluacion no encontrada"));

        evaluacion.setTitulo(evaluacionDTO.getTitulo());
        evaluacion.setPuntajeAprobacion(evaluacionDTO.getPuntajeAprobacion());

        if (evaluacionDTO.getIdEvento() != null) {
            Evento evento = _eventoRepository.findById(evaluacionDTO.getIdEvento())
                    .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
            evaluacion.setEvento(evento);
        }

        evaluacion = _evaluacionRepository.save(evaluacion);
        EvaluacionDTO dto = _modelMapper.map(evaluacion, EvaluacionDTO.class);
        dto.setIdEvento(evaluacion.getEvento().getIdEvento());
        return dto;
    }

    @Override
    public String inactivarEvaluacion(Long idEvaluacion) {
        Evaluacion evaluacion = _evaluacionRepository.findById(idEvaluacion)
                .orElseThrow(() -> new RuntimeException("Evaluacion no encontrada"));
        _evaluacionRepository.delete(evaluacion);
        return "Evaluacion inactivada correctamente";
    }

    @Override
    public boolean borrarEvaluacion(Long idEvaluacion) {
        Evaluacion evaluacion = _evaluacionRepository.findById(idEvaluacion)
                .orElseThrow(() -> new RuntimeException("Evaluacion no encontrada"));
        _evaluacionRepository.delete(evaluacion);
        return true;
    }
}
