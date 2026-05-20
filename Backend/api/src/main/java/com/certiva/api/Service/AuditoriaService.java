package com.certiva.api.Service;

import java.util.List;

import com.certiva.api.DTO.AuditoriaResumenDTO;
import com.certiva.api.Entity.Usuario;

public interface AuditoriaService {

    void registrarAuditoria(String accion, String descripcion, String ip, Usuario usuario);

    /**
     * Auditoría HTTP estructurada (filtro global): acción = método + ruta aproximada.
     */
    void registrarAuditoriaHttp(String accion, String entidadAfectada, String descripcion, String ip, Usuario usuario);

    List<AuditoriaResumenDTO> listarUltimasAuditorias(int limite);
}
