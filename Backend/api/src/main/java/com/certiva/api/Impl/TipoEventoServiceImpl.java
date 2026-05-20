package com.certiva.api.Impl;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.certiva.api.DTO.CrearTipoEventoDTO;
import com.certiva.api.DTO.TipoEventoDTO;
import com.certiva.api.Entity.TipoEvento;
import com.certiva.api.Repository.TipoEventoRepository;
import com.certiva.api.Service.TipoEventoService;

@Service
public class TipoEventoServiceImpl implements TipoEventoService {

    private final TipoEventoRepository _tipoEventoRepository;
    private final ModelMapper _modelMapper;

    public TipoEventoServiceImpl(TipoEventoRepository tipoEventoRepository, ModelMapper modelMapper) {
        this._tipoEventoRepository = tipoEventoRepository;
        this._modelMapper = modelMapper;
    }

    @Override
    public CrearTipoEventoDTO crearTipoEvento(CrearTipoEventoDTO tipoEventoDTO) {
        TipoEvento tipoEvento = _modelMapper.map(tipoEventoDTO, TipoEvento.class);
        tipoEvento = _tipoEventoRepository.save(tipoEvento);
        return _modelMapper.map(tipoEvento, CrearTipoEventoDTO.class);
    }

    @Override
    public List<TipoEventoDTO> listarTipoEventos() {
        List<TipoEvento> listado = _tipoEventoRepository.findAll();
        return listado.stream()
                .map(te -> _modelMapper.map(te, TipoEventoDTO.class))
                .toList();
    }

    @Override
    public TipoEventoDTO actualizarTipoEvento(TipoEventoDTO tipoEventoDTO) {
        TipoEvento tipoEvento = _tipoEventoRepository.findById(tipoEventoDTO.getIdTipoEvento())
                .orElseThrow(() -> new RuntimeException("Tipo de evento no encontrado"));

        tipoEvento.setNombre(tipoEventoDTO.getNombre());
        tipoEvento.setDescripcion(tipoEventoDTO.getDescripcion());
        tipoEvento.setTieneEvaluacion(tipoEventoDTO.getTieneEvaluacion());
        tipoEvento.setTieneGanador(tipoEventoDTO.getTieneGanador());

        tipoEvento = _tipoEventoRepository.save(tipoEvento);
        return _modelMapper.map(tipoEvento, TipoEventoDTO.class);
    }

    @Override
    public String inactivarTipoEvento(Long idTipoEvento) {
        TipoEvento tipoEvento = _tipoEventoRepository.findById(idTipoEvento)
                .orElseThrow(() -> new RuntimeException("Tipo de evento no encontrado"));
        _tipoEventoRepository.delete(tipoEvento);
        return "Tipo de evento inactivado correctamente";
    }

    @Override
    public boolean borrarTipoEvento(Long idTipoEvento) {
        TipoEvento tipoEvento = _tipoEventoRepository.findById(idTipoEvento)
                .orElseThrow(() -> new RuntimeException("Tipo de evento no encontrado"));
        _tipoEventoRepository.delete(tipoEvento);
        return true;
    }
}
