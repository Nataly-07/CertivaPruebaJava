package com.certiva.api.Impl;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.certiva.api.DTO.CrearResultadoEvaluacionDTO;
import com.certiva.api.DTO.ResultadoEvaluacionDTO;
import com.certiva.api.Entity.Inscripcion;
import com.certiva.api.Entity.ResultadoEvaluacion;
import com.certiva.api.Repository.InscripcionRepository;
import com.certiva.api.Repository.ResultadoEvaluacionRepository;
import com.certiva.api.Service.ResultadoEvaluacionService;

@Service
public class ResultadoEvaluacionServiceImpl implements ResultadoEvaluacionService {

    private final ResultadoEvaluacionRepository _resultadoEvaluacionRepository;
    private final InscripcionRepository _inscripcionRepository;
    private final ModelMapper _modelMapper;

    public ResultadoEvaluacionServiceImpl(ResultadoEvaluacionRepository resultadoEvaluacionRepository,
                                          InscripcionRepository inscripcionRepository,
                                          ModelMapper modelMapper) {
        this._resultadoEvaluacionRepository = resultadoEvaluacionRepository;
        this._inscripcionRepository = inscripcionRepository;
        this._modelMapper = modelMapper;
    }

    @Override
    public CrearResultadoEvaluacionDTO crearResultadoEvaluacion(CrearResultadoEvaluacionDTO resultadoDTO) {
        ResultadoEvaluacion resultado = new ResultadoEvaluacion();
        resultado.setNota(resultadoDTO.getNota());
        resultado.setAprobado(resultadoDTO.getNota() >= 3.0);

        Inscripcion inscripcion = _inscripcionRepository.findById(resultadoDTO.getIdInscripcion())
                .orElseThrow(() -> new RuntimeException("Inscripcion no encontrada"));
        resultado.setInscripcion(inscripcion);

        resultado = _resultadoEvaluacionRepository.save(resultado);
        return _modelMapper.map(resultado, CrearResultadoEvaluacionDTO.class);
    }

    @Override
    public List<ResultadoEvaluacionDTO> listarResultadoEvaluaciones() {
        List<ResultadoEvaluacion> listado = _resultadoEvaluacionRepository.findAll();
        return listado.stream()
                .map(r -> {
                    ResultadoEvaluacionDTO dto = _modelMapper.map(r, ResultadoEvaluacionDTO.class);
                    dto.setIdInscripcion(r.getInscripcion().getIdInscripcion());
                    return dto;
                })
                .toList();
    }

    @Override
    public ResultadoEvaluacionDTO actualizarResultadoEvaluacion(ResultadoEvaluacionDTO resultadoDTO) {
        ResultadoEvaluacion resultado = _resultadoEvaluacionRepository.findById(resultadoDTO.getId())
                .orElseThrow(() -> new RuntimeException("Resultado de evaluacion no encontrado"));

        resultado.setNota(resultadoDTO.getNota());
        resultado.setAprobado(resultadoDTO.getAprobado());

        if (resultadoDTO.getIdInscripcion() != null) {
            Inscripcion inscripcion = _inscripcionRepository.findById(resultadoDTO.getIdInscripcion())
                    .orElseThrow(() -> new RuntimeException("Inscripcion no encontrada"));
            resultado.setInscripcion(inscripcion);
        }

        resultado = _resultadoEvaluacionRepository.save(resultado);
        ResultadoEvaluacionDTO dto = _modelMapper.map(resultado, ResultadoEvaluacionDTO.class);
        dto.setIdInscripcion(resultado.getInscripcion().getIdInscripcion());
        return dto;
    }

    @Override
    public String inactivarResultadoEvaluacion(Long id) {
        ResultadoEvaluacion resultado = _resultadoEvaluacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resultado de evaluacion no encontrado"));
        _resultadoEvaluacionRepository.delete(resultado);
        return "Resultado de evaluacion inactivado correctamente";
    }

    @Override
    public boolean borrarResultadoEvaluacion(Long id) {
        ResultadoEvaluacion resultado = _resultadoEvaluacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resultado de evaluacion no encontrado"));
        _resultadoEvaluacionRepository.delete(resultado);
        return true;
    }
}
