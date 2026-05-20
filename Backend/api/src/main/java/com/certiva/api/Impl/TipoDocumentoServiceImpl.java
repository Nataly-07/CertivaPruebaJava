package com.certiva.api.Impl;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.certiva.api.DTO.CrearTipoDocumentoDTO;
import com.certiva.api.DTO.TipoDocumentoDTO;
import com.certiva.api.Entity.TipoDocumento;
import com.certiva.api.Repository.TipoDocumentoRepository;
import com.certiva.api.Service.TipoDocumentoService;

@Service
public class TipoDocumentoServiceImpl implements TipoDocumentoService {

    private final TipoDocumentoRepository _tipoDocumentoRepository;
    private final ModelMapper _modelMapper;

    public TipoDocumentoServiceImpl(TipoDocumentoRepository tipoDocumentoRepository, ModelMapper modelMapper) {
        this._tipoDocumentoRepository = tipoDocumentoRepository;
        this._modelMapper = modelMapper;
    }

    @Override
    public CrearTipoDocumentoDTO crearTipoDocumento(CrearTipoDocumentoDTO tipoDocumentoDTO) {
        TipoDocumento tipoDocumento = _modelMapper.map(tipoDocumentoDTO, TipoDocumento.class);
        tipoDocumento = _tipoDocumentoRepository.save(tipoDocumento);
        return _modelMapper.map(tipoDocumento, CrearTipoDocumentoDTO.class);
    }

    @Override
    public List<TipoDocumentoDTO> listarTipoDocumentos() {
        List<TipoDocumento> listado = _tipoDocumentoRepository.findAll();
        return listado.stream()
                .map(td -> _modelMapper.map(td, TipoDocumentoDTO.class))
                .toList();
    }

    @Override
    public TipoDocumentoDTO actualizarTipoDocumento(TipoDocumentoDTO tipoDocumentoDTO) {
        TipoDocumento tipoDocumento = _tipoDocumentoRepository.findById(tipoDocumentoDTO.getIdTipoDocumento())
                .orElseThrow(() -> new RuntimeException("Tipo de documento no encontrado"));

        tipoDocumento.setNombre(tipoDocumentoDTO.getNombre());
        tipoDocumento.setTipoDocumento(tipoDocumentoDTO.getTipoDocumento());

        tipoDocumento = _tipoDocumentoRepository.save(tipoDocumento);
        return _modelMapper.map(tipoDocumento, TipoDocumentoDTO.class);
    }

    @Override
    public String inactivarTipoDocumento(Long idTipoDocumento) {
        TipoDocumento tipoDocumento = _tipoDocumentoRepository.findById(idTipoDocumento)
                .orElseThrow(() -> new RuntimeException("Tipo de documento no encontrado"));
        _tipoDocumentoRepository.delete(tipoDocumento);
        return "Tipo de documento inactivado correctamente";
    }

    @Override
    public boolean borrarTipoDocumento(Long idTipoDocumento) {
        TipoDocumento tipoDocumento = _tipoDocumentoRepository.findById(idTipoDocumento)
                .orElseThrow(() -> new RuntimeException("Tipo de documento no encontrado"));
        _tipoDocumentoRepository.delete(tipoDocumento);
        return true;
    }
}
