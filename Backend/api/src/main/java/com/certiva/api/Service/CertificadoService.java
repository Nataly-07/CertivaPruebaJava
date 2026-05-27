package com.certiva.api.Service;

import java.util.List;
import java.util.Optional;

import com.certiva.api.DTO.CertificadoDTO;
import com.certiva.api.DTO.CertificadoAdminFilaDTO;
import com.certiva.api.DTO.CertificadosAdminVistaDTO;
import com.certiva.api.DTO.CertificadoPortalDTO;
import com.certiva.api.DTO.CertificadoVerificacionDTO;
import com.certiva.api.DTO.CrearCertificadoDTO;

public interface CertificadoService {

    CrearCertificadoDTO crearCertificado(CrearCertificadoDTO certificadoDTO);

    List<CertificadoDTO> listarCertificados();

    CertificadoDTO actualizarCertificado(CertificadoDTO certificadoDTO);

    String inactivarCertificado(Long idCertificado);

    boolean borrarCertificado(Long idCertificado);

    CertificadoDTO emitirCertificadoPorAsistencia(Long idInscripcion);

    Optional<CertificadoDTO> buscarPorUsuarioYEvento(Long idUsuario, Long idEvento);

    CertificadoVerificacionDTO verificarPublicoPorCodigo(String codigoValidacion);

    List<CertificadoPortalDTO> listarMisCertificados();

    byte[] descargarPdfMiCertificado(Long idCertificado);

    CertificadoDTO emitirCertificadoMiInscripcion(Long idInscripcion);

    CertificadosAdminVistaDTO obtenerVistaAdmin(String busqueda, Long idEvento);

    byte[] descargarPdfAdmin(Long idCertificado);

    CertificadoAdminFilaDTO revocarCertificado(Long idCertificado);
}
