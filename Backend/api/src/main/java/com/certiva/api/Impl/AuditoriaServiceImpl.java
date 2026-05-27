package com.certiva.api.Impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.certiva.api.DTO.AuditoriaResumenDTO;
import com.certiva.api.Entity.Auditoria;
import com.certiva.api.Entity.Usuario;
import com.certiva.api.Repository.AuditoriaRepository;
import com.certiva.api.Service.AuditoriaService;

@Service
public class AuditoriaServiceImpl implements AuditoriaService {

    private final AuditoriaRepository _auditoriaRepository;

    public AuditoriaServiceImpl(AuditoriaRepository auditoriaRepository) {
        this._auditoriaRepository = auditoriaRepository;
    }

    @Override
    public void registrarAuditoria(String accion, String descripcion, String ip, Usuario usuario) {
        Auditoria auditoria = Auditoria.builder()
                .accion(accion)
                .descripcion(descripcion)
                .ip(ip)
                .fecha(LocalDateTime.now())
                .usuario(usuario)
                .build();
        _auditoriaRepository.save(auditoria);
    }

    @Override
    public void registrarAuditoriaHttp(String accion, String entidadAfectada, String descripcion, String ip,
                                         Usuario usuario) {
        Auditoria auditoria = Auditoria.builder()
                .accion(accion)
                .entidadAfectada(entidadAfectada)
                .descripcion(descripcion)
                .ip(ip)
                .fecha(LocalDateTime.now())
                .usuario(usuario)
                .build();
        _auditoriaRepository.save(auditoria);
    }

    @Override
    public List<AuditoriaResumenDTO> listarUltimasAuditorias(int limite) {
        return listarConFiltros(null, null, null, null, limite);
    }

    @Override
    public List<AuditoriaResumenDTO> listarConFiltros(
            String accion, LocalDateTime desde, LocalDateTime hasta, String busqueda, int limite) {
        int n = Math.max(1, Math.min(limite <= 0 ? 100 : limite, 500));
        List<Auditoria> filas = _auditoriaRepository.buscarConFiltros(
                blankToNull(accion),
                desde,
                hasta,
                blankToNull(busqueda),
                PageRequest.of(0, n));
        return filas.stream().map(this::mapearResumen).toList();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private AuditoriaResumenDTO mapearResumen(Auditoria a) {
        Long idUsuario = (a.getUsuario() != null) ? a.getUsuario().getIdUsuario() : null;
        return AuditoriaResumenDTO.builder()
                .idAuditoria(a.getIdAuditoria())
                .accion(a.getAccion())
                .entidadAfectada(a.getEntidadAfectada())
                .descripcion(a.getDescripcion())
                .ip(a.getIp())
                .fecha(a.getFecha())
                .idUsuario(idUsuario)
                .build();
    }
}
