package com.certiva.api.Service;

import com.certiva.api.DTO.EventoCierreResultadoDTO;

public interface EventoCicloVidaService {

    void sincronizarEstadosAutomaticos();

    void cancelarEvento(Long idEvento);

    void iniciarRevision(Long idEvento);

    EventoCierreResultadoDTO cerrarEventoYCertificar(Long idEvento);

    EventoCierreResultadoDTO forzarCierreAdministrador(Long idEvento);
}
