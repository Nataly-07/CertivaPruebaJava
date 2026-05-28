package com.certiva.api.Impl;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.certiva.api.DTO.EventoCierreResultadoDTO;
import com.certiva.api.Entity.Evento;
import com.certiva.api.Entity.Inscripcion;
import com.certiva.api.Entity.Usuario;
import com.certiva.api.Exception.OperacionNoPermitidaException;
import com.certiva.api.Exception.RecursoNoEncontradoException;
import com.certiva.api.Repository.EventoRepository;
import com.certiva.api.Repository.InscripcionRepository;
import com.certiva.api.Service.AuditoriaService;
import com.certiva.api.Service.CertificadoService;
import com.certiva.api.Service.EventoCicloVidaService;
import com.certiva.api.Util.EventoEdicionPolicy;
import com.certiva.api.Util.EstadoOperativoEventoHelper;
import com.certiva.api.Util.ProfesorEventoAccessHelper;
import com.certiva.api.Util.InscripcionEstadoHelper;
import com.certiva.api.Util.SecurityUsuarioHelper;
import com.certiva.api.enums.AuditoriaAccion;
import com.certiva.api.enums.EstadoOperativoEvento;

@Service
public class EventoCicloVidaServiceImpl implements EventoCicloVidaService {

    private static final EnumSet<EstadoOperativoEvento> ESTADOS_AUTO = EnumSet.of(
            EstadoOperativoEvento.PROXIMO,
            EstadoOperativoEvento.EN_CURSO,
            EstadoOperativoEvento.FINALIZADO_POR_TIEMPO);

    private final EventoRepository eventoRepository;
    private final InscripcionRepository inscripcionRepository;
    private final CertificadoService certificadoService;
    private final AuditoriaService auditoriaService;
    private final SecurityUsuarioHelper securityUsuarioHelper;

    public EventoCicloVidaServiceImpl(EventoRepository eventoRepository,
                                       InscripcionRepository inscripcionRepository,
                                       CertificadoService certificadoService,
                                       AuditoriaService auditoriaService,
                                       SecurityUsuarioHelper securityUsuarioHelper) {
        this.eventoRepository = eventoRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.certificadoService = certificadoService;
        this.auditoriaService = auditoriaService;
        this.securityUsuarioHelper = securityUsuarioHelper;
    }

    @Override
    @Transactional
    public void sincronizarEstadosAutomaticos() {
        LocalDateTime ahora = LocalDateTime.now();
        List<Evento> eventos = eventoRepository.findAll().stream()
                .filter(e -> e.getEstadoOperativo() != null && ESTADOS_AUTO.contains(e.getEstadoOperativo()))
                .filter(e -> Boolean.TRUE.equals(e.getEstado()))
                .toList();
        for (Evento evento : eventos) {
            EstadoOperativoEventoHelper.sincronizarAutomatico(evento, ahora);
            eventoRepository.save(evento);
        }
    }

    @Override
    @Transactional
    public void cancelarEvento(Long idEvento) {
        asegurarAdmin();
        Evento evento = cargarEvento(idEvento);
        if (evento.getEstadoOperativo() == EstadoOperativoEvento.CERRADO_Y_CERTIFICADO) {
            throw new OperacionNoPermitidaException("No se puede cancelar un evento ya cerrado y certificado.");
        }
        Usuario actor = securityUsuarioHelper.usuarioAutenticado();
        evento.setEstado(false);
        evento.setEstadoOperativo(EstadoOperativoEvento.EVENT_CANCELLED);
        eventoRepository.save(evento);
        auditoriaService.registrarAuditoria(
                AuditoriaAccion.EVENT_CANCELLED,
                "Evento cancelado (soft delete): \"" + evento.getNombreEvento() + "\" id=" + idEvento,
                null,
                actor);
    }

    @Override
    @Transactional
    public void iniciarRevision(Long idEvento) {
        asegurarProfesorOAdmin();
        Evento evento = cargarEventoConStaff(idEvento);
        if (!EventoEdicionPolicy.esAdminAutenticado()) {
            ProfesorEventoAccessHelper.asegurarGestionEvento(evento, securityUsuarioHelper.usuarioAutenticado());
        }
        sincronizarSiAutomatico(evento);
        if (evento.getEstadoOperativo() != EstadoOperativoEvento.FINALIZADO_POR_TIEMPO) {
            throw new OperacionNoPermitidaException(
                    "Solo puede iniciar revisión cuando el evento está finalizado por tiempo.");
        }
        Usuario actor = securityUsuarioHelper.usuarioAutenticado();
        evento.setEstadoOperativo(EstadoOperativoEvento.EN_REVISION);
        eventoRepository.save(evento);
        auditoriaService.registrarAuditoria(
                AuditoriaAccion.EVENT_REVISION_STARTED,
                "Revisión académica iniciada: \"" + evento.getNombreEvento() + "\"",
                null,
                actor);
    }

    @Override
    @Transactional
    public EventoCierreResultadoDTO cerrarEventoYCertificar(Long idEvento) {
        asegurarProfesorOAdmin();
        Evento evento = cargarEventoConStaff(idEvento);
        if (!EventoEdicionPolicy.esAdminAutenticado()) {
            ProfesorEventoAccessHelper.asegurarGestionEvento(evento, securityUsuarioHelper.usuarioAutenticado());
        }
        return ejecutarCierre(idEvento, false);
    }

    @Override
    @Transactional
    public EventoCierreResultadoDTO forzarCierreAdministrador(Long idEvento) {
        asegurarAdmin();
        return ejecutarCierre(idEvento, true);
    }

    private EventoCierreResultadoDTO ejecutarCierre(Long idEvento, boolean forzado) {
        Evento evento = cargarEvento(idEvento);
        sincronizarSiAutomatico(evento);
        EstadoOperativoEvento op = evento.getEstadoOperativo();

        if (forzado) {
            if (op == EstadoOperativoEvento.CERRADO_Y_CERTIFICADO
                    || op == EstadoOperativoEvento.EVENT_CANCELLED) {
                throw new OperacionNoPermitidaException("El evento ya está en un estado terminal.");
            }
            if (op == EstadoOperativoEvento.FINALIZADO_POR_TIEMPO) {
                evento.setEstadoOperativo(EstadoOperativoEvento.EN_REVISION);
            }
        } else if (op != EstadoOperativoEvento.EN_REVISION) {
            throw new OperacionNoPermitidaException(
                    "Debe iniciar la revisión y validar requisitos antes de cerrar el evento.");
        }

        Usuario actor = securityUsuarioHelper.usuarioAutenticado();
        int emitidos = 0;
        int pendientes = 0;

        List<Inscripcion> inscripciones = inscripcionRepository.findByEvento_IdEvento(idEvento);
        for (Inscripcion ins : inscripciones) {
            if (InscripcionEstadoHelper.esCancelada(ins.getEstado())) {
                continue;
            }
            if (!InscripcionEstadoHelper.tieneAsistenciaConfirmada(ins.getEstado())) {
                pendientes++;
                continue;
            }
            try {
                certificadoService.emitirCertificadoPorAsistencia(ins.getIdInscripcion());
                emitidos++;
                auditoriaService.registrarAuditoria(
                        AuditoriaAccion.CERTIFICATE_GENERATED,
                        "Certificado emitido en cierre — inscripción " + ins.getIdInscripcion(),
                        null,
                        actor);
            } catch (OperacionNoPermitidaException ex) {
                pendientes++;
            }
        }

        evento.setEstadoOperativo(EstadoOperativoEvento.CERRADO_Y_CERTIFICADO);
        eventoRepository.save(evento);

        String accion = forzado ? AuditoriaAccion.EVENT_FORCE_CLOSED : AuditoriaAccion.EVENT_CLOSED;
        auditoriaService.registrarAuditoria(
                accion,
                "Cierre de evento \"" + evento.getNombreEvento() + "\" — certificados emitidos: " + emitidos,
                null,
                actor);

        return EventoCierreResultadoDTO.builder()
                .idEvento(idEvento)
                .estadoOperativo(EstadoOperativoEvento.CERRADO_Y_CERTIFICADO.name())
                .certificadosEmitidos(emitidos)
                .inscripcionesPendientesCertificado(pendientes)
                .mensaje("Evento cerrado. Certificados emitidos: " + emitidos
                        + (pendientes > 0 ? ". Pendientes sin certificar: " + pendientes : "."))
                .build();
    }

    private void sincronizarSiAutomatico(Evento evento) {
        if (evento.getEstadoOperativo() != null
                && ESTADOS_AUTO.contains(evento.getEstadoOperativo())) {
            EstadoOperativoEventoHelper.sincronizarAutomatico(evento, LocalDateTime.now());
            eventoRepository.save(evento);
        }
    }

    private Evento cargarEvento(Long idEvento) {
        return eventoRepository.findById(idEvento)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento no encontrado"));
    }

    private Evento cargarEventoConStaff(Long idEvento) {
        Evento evento = cargarEvento(idEvento);
        eventoRepository.findByIdConProfesores(idEvento)
                .ifPresent(e -> evento.setProfesoresColaboradores(e.getProfesoresColaboradores()));
        return evento;
    }

    private void asegurarAdmin() {
        if (!EventoEdicionPolicy.esAdminAutenticado()) {
            throw new OperacionNoPermitidaException("Solo un administrador puede realizar esta acción.");
        }
    }

    private void asegurarProfesorOAdmin() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new OperacionNoPermitidaException("Debe iniciar sesión.");
        }
        boolean ok = auth.getAuthorities().stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .anyMatch(a -> "ROLE_ADMIN".equals(a) || "ROLE_PROFESOR".equals(a));
        if (!ok) {
            throw new OperacionNoPermitidaException("Solo profesor o administrador puede realizar esta acción.");
        }
    }
}
