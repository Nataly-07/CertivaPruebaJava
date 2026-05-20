package com.certiva.api.Impl;

import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.certiva.api.DTO.CrearPagoDTO;
import com.certiva.api.DTO.PagoDTO;
import com.certiva.api.Entity.Inscripcion;
import com.certiva.api.Entity.Pago;
import com.certiva.api.Repository.InscripcionRepository;
import com.certiva.api.Repository.PagoRepository;
import com.certiva.api.Service.PagoService;

@Service
public class PagoServiceImpl implements PagoService {

    private final PagoRepository _pagoRepository;
    private final InscripcionRepository _inscripcionRepository;
    private final ModelMapper _modelMapper;

    public PagoServiceImpl(PagoRepository pagoRepository,
                           InscripcionRepository inscripcionRepository,
                           ModelMapper modelMapper) {
        this._pagoRepository = pagoRepository;
        this._inscripcionRepository = inscripcionRepository;
        this._modelMapper = modelMapper;
    }

    @Override
    public CrearPagoDTO crearPago(CrearPagoDTO pagoDTO) {
        Pago pago = new Pago();
        pago.setMonto(pagoDTO.getMonto());
        pago.setEstado("PENDIENTE");
        pago.setMetodoPago(pagoDTO.getMetodoPago());
        pago.setFechaPago(LocalDateTime.now());

        Inscripcion inscripcion = _inscripcionRepository.findById(pagoDTO.getIdInscripcion())
                .orElseThrow(() -> new RuntimeException("Inscripcion no encontrada"));
        pago.setInscripcion(inscripcion);

        pago = _pagoRepository.save(pago);
        return _modelMapper.map(pago, CrearPagoDTO.class);
    }

    @Override
    public List<PagoDTO> listarPagos() {
        List<Pago> listado = _pagoRepository.findAll();
        return listado.stream()
                .map(p -> {
                    PagoDTO dto = _modelMapper.map(p, PagoDTO.class);
                    dto.setIdInscripcion(p.getInscripcion().getIdInscripcion());
                    return dto;
                })
                .toList();
    }

    @Override
    public PagoDTO actualizarPago(PagoDTO pagoDTO) {
        Pago pago = _pagoRepository.findById(pagoDTO.getIdPago())
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));

        pago.setMonto(pagoDTO.getMonto());
        pago.setEstado(pagoDTO.getEstado());
        pago.setMetodoPago(pagoDTO.getMetodoPago());

        if (pagoDTO.getIdInscripcion() != null) {
            Inscripcion inscripcion = _inscripcionRepository.findById(pagoDTO.getIdInscripcion())
                    .orElseThrow(() -> new RuntimeException("Inscripcion no encontrada"));
            pago.setInscripcion(inscripcion);
        }

        pago = _pagoRepository.save(pago);
        PagoDTO dto = _modelMapper.map(pago, PagoDTO.class);
        dto.setIdInscripcion(pago.getInscripcion().getIdInscripcion());
        return dto;
    }

    @Override
    public String inactivarPago(Long idPago) {
        Pago pago = _pagoRepository.findById(idPago)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));
        pago.setEstado("CANCELADO");
        _pagoRepository.save(pago);
        return "Pago inactivado correctamente";
    }

    @Override
    public boolean borrarPago(Long idPago) {
        Pago pago = _pagoRepository.findById(idPago)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));
        _pagoRepository.delete(pago);
        return true;
    }
}
