package com.certiva.api.Service;

import java.util.List;

import com.certiva.api.DTO.CrearTipoDocumentoDTO;
import com.certiva.api.DTO.TipoDocumentoDTO;

public interface TipoDocumentoService {

    CrearTipoDocumentoDTO crearTipoDocumento(CrearTipoDocumentoDTO tipoDocumentoDTO);

    List<TipoDocumentoDTO> listarTipoDocumentos();

    TipoDocumentoDTO actualizarTipoDocumento(TipoDocumentoDTO tipoDocumentoDTO);

    String inactivarTipoDocumento(Long idTipoDocumento);

    boolean borrarTipoDocumento(Long idTipoDocumento);
}
