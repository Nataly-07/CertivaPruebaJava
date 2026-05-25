package com.certiva.api.Impl;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.certiva.api.DTO.CertificadoDTO;
import com.certiva.api.DTO.CheckInRespuestaDTO;
import com.certiva.api.DTO.CrearInscripcionDTO;
import com.certiva.api.DTO.InscripcionDTO;
import com.certiva.api.DTO.InscripcionPortalDTO;
import com.certiva.api.DTO.RespuestaCampoDTO;
import com.certiva.api.Entity.Certificado;
import com.certiva.api.Entity.CampoFormulario;
import com.certiva.api.Entity.Evento;
import com.certiva.api.Entity.Inscripcion;
import com.certiva.api.Entity.RespuestaFormulario;
import com.certiva.api.Entity.Usuario;
import com.certiva.api.Exception.ConflictoOperacionException;
import com.certiva.api.Exception.OperacionNoPermitidaException;
import com.certiva.api.Exception.RecursoNoEncontradoException;
import com.certiva.api.Repository.CampoFormularioRepository;
import com.certiva.api.Repository.CertificadoRepository;
import com.certiva.api.Repository.InscripcionRepository;
import com.certiva.api.Repository.UsuarioRepository;
import com.certiva.api.Repository.EventoRepository;
import com.certiva.api.Util.EstadoOperativoEventoHelper;
import com.certiva.api.Util.EventoEdicionPolicy;
import com.certiva.api.Util.InscripcionEstadoHelper;
import com.certiva.api.Util.InscripcionQrHelper;
import com.certiva.api.Util.SecurityUsuarioHelper;
import com.certiva.api.Service.AuditoriaService;
import com.certiva.api.Service.CertificadoElegibilidadService;
import com.certiva.api.Service.CertificadoService;
import com.certiva.api.Service.EventoService;
import com.certiva.api.Service.InscripcionService;
import com.certiva.api.enums.AuditoriaAccion;
import com.certiva.api.enums.EstadoAsistenciaCheckIn;
import com.certiva.api.enums.EstadoOperativoEvento;
import com.certiva.api.enums.TipoDatoCampo;

@Service
public class InscripcionServiceImpl implements InscripcionService {

    private final InscripcionRepository _inscripcionRepository;
    private final UsuarioRepository _usuarioRepository;
    private final EventoRepository _eventoRepository;
    private final CampoFormularioRepository _campoFormularioRepository;
    private final CertificadoRepository _certificadoRepository;
    private final CertificadoService _certificadoService;
    private final CertificadoElegibilidadService _elegibilidadService;
    private final EventoService _eventoService;
    private final SecurityUsuarioHelper _securityUsuarioHelper;
    private final ModelMapper _modelMapper;
    private final ObjectMapper _objectMapper;
    private final String _publicApiBaseUrl;
    private final AuditoriaService _auditoriaService;

    public InscripcionServiceImpl(InscripcionRepository inscripcionRepository,
                                  UsuarioRepository usuarioRepository,
                                  EventoRepository eventoRepository,
                                  CampoFormularioRepository campoFormularioRepository,
                                  CertificadoRepository certificadoRepository,
                                  CertificadoService certificadoService,
                                  CertificadoElegibilidadService elegibilidadService,
                                  EventoService eventoService,
                                  SecurityUsuarioHelper securityUsuarioHelper,
                                  ModelMapper modelMapper,
                                  ObjectMapper objectMapper,
                                  AuditoriaService auditoriaService,
                                  @Value("${certiva.api.public-base-url:http://localhost:8080}") String publicApiBaseUrl) {
        this._inscripcionRepository = inscripcionRepository;
        this._usuarioRepository = usuarioRepository;
        this._eventoRepository = eventoRepository;
        this._campoFormularioRepository = campoFormularioRepository;
        this._certificadoRepository = certificadoRepository;
        this._certificadoService = certificadoService;
        this._elegibilidadService = elegibilidadService;
        this._eventoService = eventoService;
        this._securityUsuarioHelper = securityUsuarioHelper;
        this._modelMapper = modelMapper;
        this._objectMapper = objectMapper;
        this._publicApiBaseUrl = publicApiBaseUrl;
        this._auditoriaService = auditoriaService;
    }

    @Override
    @Transactional
    public CrearInscripcionDTO crearInscripcion(CrearInscripcionDTO inscripcionDTO) {
        Usuario usuario = _usuarioRepository.findById(inscripcionDTO.getIdUsuario())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        if (usuario.getTelefono() == null || usuario.getTelefono().isBlank()) {
            throw new ConflictoOperacionException(
                    "Debe registrar un teléfono de contacto en su perfil antes de inscribirse.");
        }

        Evento evento = _eventoRepository.findById(inscripcionDTO.getIdEvento())
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento no encontrado"));

        if (Boolean.FALSE.equals(evento.getEstado())) {
            throw new ConflictoOperacionException("El evento no está activo.");
        }
        EstadoOperativoEvento op = EstadoOperativoEventoHelper.resolverOperativoVisible(evento, LocalDateTime.now());
        if (op != EstadoOperativoEvento.PROXIMO) {
            throw new ConflictoOperacionException(
                    "Solo puede inscribirse a eventos en estado Próximo.");
        }

        if (!_eventoService.verificarCupo(evento.getIdEvento()).isHayCupoDisponible()) {
            throw new ConflictoOperacionException("Cupo del evento agotado (aforo completo).");
        }

        boolean gratuito = evento.getCosto() == null || evento.getCosto() <= 0.0;
        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setEstado(gratuito ? InscripcionEstadoHelper.APROBADA : InscripcionEstadoHelper.PENDIENTE);
        inscripcion.setPagoRealizado(gratuito);
        inscripcion.setFechaInscripcion(LocalDateTime.now());
        inscripcion.setUsuario(usuario);
        inscripcion.setEvento(evento);

        inscripcion = _inscripcionRepository.save(inscripcion);
        inscripcion.setTokenQr(String.valueOf(inscripcion.getIdInscripcion()));
        inscripcion = _inscripcionRepository.save(inscripcion);

        validarYPersistirRespuestas(evento, inscripcion, inscripcionDTO.getRespuestasCampos());

        CrearInscripcionDTO salida = _modelMapper.map(inscripcion, CrearInscripcionDTO.class);
        salida.setIdUsuario(inscripcion.getUsuario().getIdUsuario());
        salida.setIdEvento(inscripcion.getEvento().getIdEvento());
        salida.setTokenQr(inscripcion.getTokenQr());
        salida.setRespuestasCampos(inscripcionDTO.getRespuestasCampos() != null ? inscripcionDTO.getRespuestasCampos() : List.of());
        return salida;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InscripcionDTO> listarInscripciones() {
        List<Inscripcion> listado = _inscripcionRepository.findAll();
        return listado.stream()
                .map(i -> {
                    InscripcionDTO dto = _modelMapper.map(i, InscripcionDTO.class);
                    dto.setIdUsuario(i.getUsuario().getIdUsuario());
                    dto.setIdEvento(i.getEvento().getIdEvento());
                    dto.setTokenQr(i.getTokenQr());
                    dto.setRespuestasCampos(respuestasADto(i));
                    return dto;
                })
                .toList();
    }

    @Override
    @Transactional
    public InscripcionDTO actualizarInscripcion(InscripcionDTO inscripcionDTO) {
        Inscripcion inscripcion = _inscripcionRepository.findById(inscripcionDTO.getIdInscripcion())
                .orElseThrow(() -> new RecursoNoEncontradoException("Inscripción no encontrada"));

        inscripcion.setEstado(inscripcionDTO.getEstado());
        inscripcion.setPagoRealizado(inscripcionDTO.getPagoRealizado());

        if (inscripcionDTO.getIdUsuario() != null) {
            Usuario usuario = _usuarioRepository.findById(inscripcionDTO.getIdUsuario())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
            inscripcion.setUsuario(usuario);
        }

        if (inscripcionDTO.getIdEvento() != null) {
            Evento evento = _eventoRepository.findById(inscripcionDTO.getIdEvento())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Evento no encontrado"));
            inscripcion.setEvento(evento);
        }

        inscripcion = _inscripcionRepository.save(inscripcion);
        InscripcionDTO dto = _modelMapper.map(inscripcion, InscripcionDTO.class);
        dto.setIdUsuario(inscripcion.getUsuario().getIdUsuario());
        dto.setIdEvento(inscripcion.getEvento().getIdEvento());
        dto.setTokenQr(inscripcion.getTokenQr());
        dto.setRespuestasCampos(respuestasADto(inscripcion));
        return dto;
    }

    @Override
    @Transactional
    public String inactivarInscripcion(Long idInscripcion) {
        Inscripcion inscripcion = _inscripcionRepository.findById(idInscripcion)
                .orElseThrow(() -> new RecursoNoEncontradoException("Inscripción no encontrada"));
        inscripcion.setEstado(InscripcionEstadoHelper.CANCELLED);
        _inscripcionRepository.save(inscripcion);
        return "Inscripción cancelada correctamente";
    }

    @Override
    @Transactional
    public String cancelarMiInscripcion(Long idInscripcion) {
        Usuario usuario = _securityUsuarioHelper.usuarioAutenticado();
        Inscripcion inscripcion = _inscripcionRepository.findById(idInscripcion)
                .orElseThrow(() -> new RecursoNoEncontradoException("Inscripción no encontrada"));
        if (!inscripcion.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
            throw new OperacionNoPermitidaException("No puede cancelar inscripciones de otro usuario.");
        }
        Evento evento = inscripcion.getEvento();
        EstadoOperativoEvento op = EstadoOperativoEventoHelper.resolverOperativoVisible(evento, LocalDateTime.now());
        if (op != EstadoOperativoEvento.PROXIMO) {
            throw new OperacionNoPermitidaException(
                    "No puede cancelar la inscripción cuando el evento ya está en curso o finalizado.");
        }
        if (InscripcionEstadoHelper.esCancelada(inscripcion.getEstado())) {
            return "La inscripción ya estaba cancelada.";
        }
        inscripcion.setEstado(InscripcionEstadoHelper.CANCELLED);
        _inscripcionRepository.save(inscripcion);
        _auditoriaService.registrarAuditoria(
                AuditoriaAccion.INSCRIPCION_CANCELLED,
                "Inscripción " + idInscripcion + " cancelada por el estudiante",
                null,
                usuario);
        return "Inscripción cancelada; el cupo quedó liberado.";
    }

    @Override
    @Transactional
    public boolean borrarInscripcion(Long idInscripcion) {
        throw new OperacionNoPermitidaException(
                "Borrado físico prohibido. Use cancelación (soft delete).");
    }

    @Override
    @Transactional
    public CheckInRespuestaDTO confirmarAsistenciaPorCodigoQr(String codigoBruto, String tipoAsistencia) {
        Usuario operador = _securityUsuarioHelper.usuarioAutenticado();
        if (codigoBruto == null || codigoBruto.isBlank()) {
            registrarCheckinFallido(operador, "Código vacío");
            throw new RecursoNoEncontradoException("Código inválido");
        }
        String codigo = codigoBruto.trim();

        Inscripcion inscripcion = resolverInscripcionPorCodigo(codigo).orElse(null);
        if (inscripcion == null) {
            registrarCheckinFallido(operador, "Inscripción no encontrada: " + codigo);
            throw new RecursoNoEncontradoException("Inscripción no encontrada para el código");
        }

        try {
            return ejecutarCheckIn(inscripcion, tipoAsistencia, operador);
        } catch (ConflictoOperacionException | OperacionNoPermitidaException ex) {
            registrarCheckinFallido(operador, ex.getMessage());
            throw ex;
        }
    }

    private CheckInRespuestaDTO ejecutarCheckIn(Inscripcion inscripcion, String tipoAsistencia, Usuario operador) {
        Evento evento = inscripcion.getEvento();
        EstadoOperativoEvento op = EstadoOperativoEventoHelper.resolverOperativoVisible(evento, LocalDateTime.now());
        if (op != EstadoOperativoEvento.EN_CURSO) {
            throw new ConflictoOperacionException(
                    "Check-in solo permitido mientras el evento está en curso.");
        }

        validarMonitorAsignadoAlEvento(evento, operador);

        String est = InscripcionEstadoHelper.norm(inscripcion.getEstado());

        if (InscripcionEstadoHelper.tieneAsistenciaConfirmada(est)) {
            return CheckInRespuestaDTO.builder()
                    .mensaje("La asistencia ya estaba registrada (" + est + ").")
                    .idInscripcion(inscripcion.getIdInscripcion())
                    .estadoInscripcion(inscripcion.getEstado())
                    .build();
        }

        if (InscripcionEstadoHelper.esCancelada(est)) {
            throw new ConflictoOperacionException("La inscripción está cancelada.");
        }

        if (InscripcionEstadoHelper.PENDIENTE.equals(est)) {
            throw new ConflictoOperacionException(
                    "La inscripción aún no está aprobada. Complete el pago o la aprobación.");
        }

        String estadoAsistencia = resolverEstadoAsistencia(tipoAsistencia);
        inscripcion.setEstado(estadoAsistencia);
        _inscripcionRepository.save(inscripcion);

        _auditoriaService.registrarAuditoria(
                AuditoriaAccion.CHECKIN_SUCCESS,
                "Check-in " + estadoAsistencia + " — inscripción " + inscripcion.getIdInscripcion()
                        + " evento " + evento.getIdEvento(),
                null,
                operador);

        String motivoCert = _elegibilidadService.motivoPendienteCertificado(inscripcion);
        return CheckInRespuestaDTO.builder()
                .mensaje("Asistencia registrada: " + estadoAsistencia + "."
                        + (motivoCert != null ? " " + motivoCert
                        : " El certificado se emitirá al cerrar el evento."))
                .idInscripcion(inscripcion.getIdInscripcion())
                .estadoInscripcion(inscripcion.getEstado())
                .certificadoPendienteMotivo(motivoCert)
                .build();
    }

    private void validarMonitorAsignadoAlEvento(Evento evento, Usuario operador) {
        if (EventoEdicionPolicy.esAdminAutenticado()) {
            return;
        }
        boolean esMonitor = operador.getRoles().stream()
                .anyMatch(r -> "ROLE_MONITOR".equals(r.getNombre()));
        if (!esMonitor) {
            return;
        }
        _eventoRepository.findByIdConMonitores(evento.getIdEvento()).ifPresent(e -> {
            boolean asignado = e.getMonitoresAsignados().stream()
                    .anyMatch(m -> m.getIdUsuario().equals(operador.getIdUsuario()));
            if (!asignado) {
                throw new OperacionNoPermitidaException(
                        "No está asignado como monitor de este evento.");
            }
        });
    }

    private static String resolverEstadoAsistencia(String tipoAsistencia) {
        if (tipoAsistencia == null || tipoAsistencia.isBlank()) {
            return InscripcionEstadoHelper.PRESENTE;
        }
        String t = tipoAsistencia.trim().toUpperCase();
        if ("TARDIO".equals(t) || "TARDÍO".equalsIgnoreCase(tipoAsistencia.trim())) {
            return InscripcionEstadoHelper.TARDIO;
        }
        if ("AUSENTE".equals(t)) {
            return InscripcionEstadoHelper.AUSENTE;
        }
        try {
            return EstadoAsistenciaCheckIn.valueOf(t).name();
        } catch (IllegalArgumentException ex) {
            return InscripcionEstadoHelper.PRESENTE;
        }
    }

    private void registrarCheckinFallido(Usuario operador, String motivo) {
        _auditoriaService.registrarAuditoria(
                AuditoriaAccion.CHECKIN_DENIED,
                motivo,
                null,
                operador);
    }

    private List<RespuestaCampoDTO> respuestasADto(Inscripcion inscripcion) {
        if (inscripcion.getRespuestasFormulario() == null || inscripcion.getRespuestasFormulario().isEmpty()) {
            return List.of();
        }
        return inscripcion.getRespuestasFormulario().stream()
                .map(r -> new RespuestaCampoDTO(r.getCampo().getIdCampo(), r.getValor()))
                .toList();
    }

    private void validarYPersistirRespuestas(Evento evento, Inscripcion inscripcion, List<RespuestaCampoDTO> entrada) {
        List<CampoFormulario> campos = _campoFormularioRepository.findByEvento_IdEventoOrderByIdCampoAsc(evento.getIdEvento());
        Map<Long, CampoFormulario> porId = new HashMap<>();
        for (CampoFormulario c : campos) {
            porId.put(c.getIdCampo(), c);
        }

        Map<Long, String> valores = new HashMap<>();
        if (entrada != null) {
            for (RespuestaCampoDTO r : entrada) {
                if (r.getIdCampo() == null) {
                    continue;
                }
                if (!porId.containsKey(r.getIdCampo())) {
                    throw new ConflictoOperacionException("El campo " + r.getIdCampo() + " no pertenece al evento.");
                }
                String v = r.getValor() == null ? "" : r.getValor().trim();
                valores.put(r.getIdCampo(), v);
            }
        }

        for (CampoFormulario c : campos) {
            if (!c.isEsObligatorio()) {
                continue;
            }
            if (!valores.containsKey(c.getIdCampo())) {
                throw new ConflictoOperacionException("Falta respuesta para el campo obligatorio: " + c.getEtiqueta());
            }
            String v = valores.get(c.getIdCampo());
            if (v.isEmpty() && c.getTipoDato() != TipoDatoCampo.CHECKBOX) {
                throw new ConflictoOperacionException("Falta respuesta para el campo obligatorio: " + c.getEtiqueta());
            }
            if (v.isEmpty() && c.getTipoDato() == TipoDatoCampo.CHECKBOX) {
                throw new ConflictoOperacionException("Falta respuesta para el campo obligatorio: " + c.getEtiqueta());
            }
        }

        for (CampoFormulario c : campos) {
            if (!valores.containsKey(c.getIdCampo())) {
                continue;
            }
            String v = valores.get(c.getIdCampo());
            if (v.isEmpty()) {
                continue;
            }
            String normalizado = normalizarYValidarValor(c, v);
            RespuestaFormulario rf = new RespuestaFormulario();
            rf.setCampo(c);
            rf.setInscripcion(inscripcion);
            rf.setValor(normalizado);
            inscripcion.getRespuestasFormulario().add(rf);
        }
        _inscripcionRepository.save(inscripcion);
    }

    private String normalizarYValidarValor(CampoFormulario c, String v) {
        return switch (c.getTipoDato()) {
            case TEXTO -> v.trim();
            case NUMERO -> {
                try {
                    new BigDecimal(v.trim().replace(',', '.'));
                    yield v.trim();
                } catch (Exception ex) {
                    throw new ConflictoOperacionException("Valor numérico inválido en \"" + c.getEtiqueta() + "\"");
                }
            }
            case SELECT -> {
                List<String> opts = parseOpcionesAlmacenadas(c.getOpciones());
                String t = v.trim();
                if (!opts.contains(t)) {
                    throw new ConflictoOperacionException("Valor no válido para el desplegable: " + c.getEtiqueta());
                }
                yield t;
            }
            case CHECKBOX -> {
                String low = v.trim().toLowerCase();
                if ("true".equals(low) || "1".equals(low) || "sí".equals(low) || "si".equals(low)) {
                    yield "true";
                }
                if ("false".equals(low) || "0".equals(low)) {
                    yield "false";
                }
                throw new ConflictoOperacionException("Valor de casilla inválido en: " + c.getEtiqueta());
            }
            case URL -> validarUrlCampo(c.getEtiqueta(), v);
            case IMAGEN -> validarImagenCampo(c.getEtiqueta(), v);
        };
    }

    private static final int URL_MAX_LENGTH = 2048;
    private static final int IMAGEN_MAX_CHARS = 3_500_000;

    private String validarUrlCampo(String etiqueta, String v) {
        String t = v.trim();
        if (t.length() > URL_MAX_LENGTH) {
            throw new ConflictoOperacionException(
                    "La URL en \"" + etiqueta + "\" supera el máximo de " + URL_MAX_LENGTH + " caracteres.");
        }
        if (!esUrlAceptable(t)) {
            throw new ConflictoOperacionException("URL inválida en \"" + etiqueta + "\".");
        }
        return t;
    }

    private String validarImagenCampo(String etiqueta, String v) {
        String t = v.trim();
        if (t.length() > IMAGEN_MAX_CHARS) {
            throw new ConflictoOperacionException("La imagen en \"" + etiqueta + "\" es demasiado grande.");
        }
        if (t.startsWith("data:image/")) {
            if (!t.contains(";base64,")) {
                throw new ConflictoOperacionException("Formato de imagen inválido en \"" + etiqueta + "\".");
            }
            return t;
        }
        if (esUrlAceptable(t)) {
            return t;
        }
        throw new ConflictoOperacionException(
                "En \"" + etiqueta + "\" suba una imagen o indique una URL pública válida.");
    }

    private boolean esUrlAceptable(String raw) {
        if (raw.isBlank() || raw.length() > URL_MAX_LENGTH) {
            return false;
        }
        String probe = raw.matches("(?i)^https?://.*") ? raw : "https://" + raw;
        try {
            new java.net.URI(probe);
            return true;
        } catch (Exception ex) {
            return raw.contains(".") && raw.length() >= 4;
        }
    }

    private List<String> parseOpcionesAlmacenadas(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            List<String> list = _objectMapper.readValue(json.trim(), new TypeReference<List<String>>() {});
            return list != null ? list : List.of();
        } catch (Exception e) {
            throw new ConflictoOperacionException("Opciones de campo SELECT inválidas en el servidor.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<InscripcionPortalDTO> listarMisInscripciones() {
        Usuario usuario = _securityUsuarioHelper.usuarioAutenticado();
        LocalDateTime ahora = LocalDateTime.now();
        return _inscripcionRepository.findMisInscripcionesConEvento(usuario.getIdUsuario())
                .stream()
                .filter(i -> !InscripcionEstadoHelper.esCancelada(i.getEstado()))
                .map(i -> mapearPortal(i, ahora))
                .toList();
    }

    private InscripcionPortalDTO mapearPortal(Inscripcion inscripcion, LocalDateTime ahora) {
        Evento evento = inscripcion.getEvento();
        Optional<Certificado> certOpt = _certificadoRepository.findFirstByUsuario_IdUsuarioAndEvento_IdEvento(
                inscripcion.getUsuario().getIdUsuario(),
                evento.getIdEvento());

        String fase = calcularFase(ahora, evento.getFechaInicio(), evento.getFechaFin());
        EstadoOperativoEvento opEvento = EstadoOperativoEventoHelper.resolverOperativoVisible(evento, ahora);
        boolean eventoCerrado = opEvento == EstadoOperativoEvento.CERRADO_Y_CERTIFICADO;
        boolean puedeDescargar = certOpt.isPresent() && eventoCerrado;
        String motivo = null;
        if (!puedeDescargar && eventoCerrado) {
            motivo = _elegibilidadService.motivoPendienteCertificado(inscripcion);
        } else if (!puedeDescargar && opEvento == EstadoOperativoEvento.EN_REVISION) {
            motivo = "El certificado estará disponible tras el cierre oficial del evento.";
        } else if (!puedeDescargar) {
            motivo = "El certificado estará disponible al cerrar y certificar el evento.";
        }

        int sesionesTotales = calcularSesionesTotales(evento);
        int sesionesAsistidas = calcularSesionesAsistidas(inscripcion, evento, ahora, sesionesTotales);
        int porcentajeProgreso = sesionesTotales > 0
                ? Math.min(100, Math.round((sesionesAsistidas * 100f) / sesionesTotales))
                : 0;

        return InscripcionPortalDTO.builder()
                .idInscripcion(inscripcion.getIdInscripcion())
                .estado(inscripcion.getEstado())
                .tokenQr(contenidoQr(inscripcion.getIdInscripcion()))
                .fechaInscripcion(inscripcion.getFechaInscripcion())
                .idEvento(evento.getIdEvento())
                .nombreEvento(evento.getNombreEvento())
                .tipoEvento(evento.getTipoEvento() != null ? evento.getTipoEvento().name() : null)
                .modalidad(evento.getModalidad() != null ? evento.getModalidad().name() : null)
                .instructorNombre(nombreInstructor(evento))
                .enlaceVirtual(evento.getEnlaceVirtual())
                .fechaInicio(evento.getFechaInicio())
                .fechaFin(evento.getFechaFin())
                .fase(fase)
                .sesionesTotales(sesionesTotales)
                .sesionesAsistidas(sesionesAsistidas)
                .porcentajeProgreso(porcentajeProgreso)
                .puedeDescargarCertificado(puedeDescargar)
                .idCertificado(certOpt.map(Certificado::getIdCertificado).orElse(null))
                .motivoCertificadoPendiente(motivo)
                .build();
    }

    private static String nombreInstructor(Evento evento) {
        if (evento.getUsuarioCreador() == null) {
            return "Por asignar";
        }
        String n = evento.getUsuarioCreador().getNombres() != null ? evento.getUsuarioCreador().getNombres().trim() : "";
        String a = evento.getUsuarioCreador().getApellidos() != null ? evento.getUsuarioCreador().getApellidos().trim() : "";
        String completo = (n + " " + a).trim();
        return completo.isEmpty() ? "Por asignar" : completo;
    }

    /**
     * Sesiones totales: intensidad horaria (bloques de ~4 h) o duración en días del evento.
     */
    private static int calcularSesionesTotales(Evento evento) {
        if (evento.getIntensidadHoraria() != null && evento.getIntensidadHoraria() > 0) {
            return Math.max(1, (int) Math.ceil(evento.getIntensidadHoraria() / 4.0));
        }
        if (evento.getFechaInicio() != null && evento.getFechaFin() != null) {
            long dias = ChronoUnit.DAYS.between(
                    evento.getFechaInicio().toLocalDate(),
                    evento.getFechaFin().toLocalDate()) + 1;
            return (int) Math.max(1, Math.min(dias, 40));
        }
        return 1;
    }

    /**
     * Asistió (check-in) = 100 %; en curso = proporción temporal; inscrito sin iniciar = 0.
     */
    private static int calcularSesionesAsistidas(Inscripcion inscripcion, Evento evento,
                                                 LocalDateTime ahora, int sesionesTotales) {
        if (InscripcionEstadoHelper.tieneAsistenciaConfirmada(inscripcion.getEstado())) {
            return sesionesTotales;
        }
        if (evento.getFechaInicio() == null || evento.getFechaFin() == null) {
            return 0;
        }
        if (ahora.isBefore(evento.getFechaInicio())) {
            return 0;
        }
        if (!ahora.isBefore(evento.getFechaFin())) {
            return sesionesTotales;
        }
        long totalMs = Duration.between(evento.getFechaInicio(), evento.getFechaFin()).toMillis();
        if (totalMs <= 0) {
            return 0;
        }
        long elapsedMs = Duration.between(evento.getFechaInicio(), ahora).toMillis();
        int estimado = (int) Math.round((elapsedMs * (double) sesionesTotales) / totalMs);
        return Math.max(0, Math.min(sesionesTotales, estimado));
    }

    private static String calcularFase(LocalDateTime ahora, LocalDateTime inicio, LocalDateTime fin) {
        if (inicio != null && ahora.isBefore(inicio)) {
            return "INSCRITO";
        }
        if (fin != null && ahora.isAfter(fin)) {
            return "FINALIZADO";
        }
        return "EN_CURSO";
    }

    private static String norm(String estado) {
        return estado == null ? "" : estado.trim();
    }

    private String contenidoQr(Long idInscripcion) {
        return InscripcionQrHelper.buildQrContent(_publicApiBaseUrl, idInscripcion);
    }

    private Optional<Inscripcion> resolverInscripcionPorCodigo(String codigo) {
        Optional<Long> id = InscripcionQrHelper.resolveInscripcionId(codigo);
        if (id.isPresent()) {
            return _inscripcionRepository.findById(id.get());
        }
        return _inscripcionRepository.findByTokenQr(codigo);
    }
}
