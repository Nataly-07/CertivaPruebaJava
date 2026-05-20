package com.certiva.api.Service;

import java.util.List;

import com.certiva.api.DTO.CrearPagoDTO;
import com.certiva.api.DTO.PagoDTO;

public interface PagoService {

    CrearPagoDTO crearPago(CrearPagoDTO pagoDTO);

    List<PagoDTO> listarPagos();

    PagoDTO actualizarPago(PagoDTO pagoDTO);

    String inactivarPago(Long idPago);

    boolean borrarPago(Long idPago);
}
