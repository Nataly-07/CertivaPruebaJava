package com.certiva.api.Impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.certiva.api.DTO.CertificadoAdminFilaDTO;
import com.certiva.api.DTO.CertificadosAdminKpiDTO;
import com.certiva.api.DTO.CertificadosAdminVistaDTO;
import com.certiva.api.DTO.CertificadosAdminVistaDTO.EventoOpcionFiltroDTO;
import com.certiva.api.Entity.Certificado;
import com.certiva.api.Entity.Evento;
import com.certiva.api.Entity.Inscripcion;
import com.certiva.api.Entity.Usuario;
import com.certiva.api.Exception.OperacionNoPermitidaException;
import com.certiva.api.Exception.RecursoNoEncontradoException;
import com.certiva.api.Repository.CertificadoRepository;
import com.certiva.api.Repository.EventoRepository;
import com.certiva.api.Repository.InscripcionRepository;
import com.certiva.api.Repository.UsuarioRepository;
import com.certiva.api.Config.CertivaAppProperties;
import com.certiva.api.enums.EstadoCertificado;
import com.certiva.api.enums.EstadoOperativoEvento;
import com.certiva.api.DTO.CertificadoDTO;
import com.certiva.api.DTO.CertificadoPortalDTO;
import com.certiva.api.DTO.CertificadoVerificacionDTO;
import com.certiva.api.DTO.CrearCertificadoDTO;
import com.certiva.api.Service.AuditoriaService;
import com.certiva.api.Service.CertificadoElegibilidadService;
import com.certiva.api.Service.CertificadoService;
import com.certiva.api.Util.PdfCertificadoGenerator;
import com.certiva.api.Util.SecurityUsuarioHelper;

@Service
public class CertificadoServiceImpl implements CertificadoService {

    private final CertificadoRepository _certificadoRepository;
    private final UsuarioRepository _usuarioRepository;
    private final EventoRepository _eventoRepository;
    private final InscripcionRepository _inscripcionRepository;
    private final PdfCertificadoGenerator _pdfCertificadoGenerator;
    private final CertificadoElegibilidadService _elegibilidadService;
    private final CertivaAppProperties _appProperties;
    private final SecurityUsuarioHelper _securityUsuarioHelper;
    private final ModelMapper _modelMapper;
    private final AuditoriaService _auditoriaService;

    public CertificadoServiceImpl(CertificadoRepository certificadoRepository,
                                  UsuarioRepository usuarioRepository,
                                  EventoRepository eventoRepository,
                                  InscripcionRepository inscripcionRepository,
                                  PdfCertificadoGenerator pdfCertificadoGenerator,
                                  CertificadoElegibilidadService elegibilidadService,
                                  CertivaAppProperties appProperties,
                                  SecurityUsuarioHelper securityUsuarioHelper,
                                  ModelMapper modelMapper,
                                  AuditoriaService auditoriaService) {
        this._certificadoRepository = certificadoRepository;
        this._usuarioRepository = usuarioRepository;
        this._eventoRepository = eventoRepository;
        this._inscripcionRepository = inscripcionRepository;
        this._pdfCertificadoGenerator = pdfCertificadoGenerator;
        this._elegibilidadService = elegibilidadService;
        this._appProperties = appProperties;
        this._securityUsuarioHelper = securityUsuarioHelper;
        this._modelMapper = modelMapper;
        this._auditoriaService = auditoriaService;
    }

    @Override
    public CrearCertificadoDTO crearCertificado(CrearCertificadoDTO certificadoDTO) {
        Certificado certificado = new Certificado();
        certificado.setTipoCertificado(certificadoDTO.getTipoCertificado());
        certificado.setCodigoValidacion(generarCodigoUnico());
        certificado.setFechaEmision(LocalDateTime.now());
        certificado.setEstado(EstadoCertificado.VALIDO);

        Usuario usuario = _usuarioRepository.findById(certificadoDTO.getIdUsuario())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        certificado.setUsuario(usuario);

        Evento evento = _eventoRepository.findById(certificadoDTO.getIdEvento())
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento no encontrado"));
        certificado.setEvento(evento);

        String urlVerificacion = _appProperties.urlVerificarCertificado(certificado.getCodigoValidacion());
        byte[] pdf = _pdfCertificadoGenerator.generarPdf(
                evento.getNombreEvento(),
                usuario.getNombres() + " " + usuario.getApellidos(),
                certificado.getCodigoValidacion(),
                urlVerificacion);
        certificado.setContenidoPdf(pdf);

        certificado = _certificadoRepository.save(certificado);
        CrearCertificadoDTO salida = _modelMapper.map(certificado, CrearCertificadoDTO.class);
        salida.setCodigoValidacion(certificado.getCodigoValidacion());
        return salida;
    }

    @Override
    public List<CertificadoDTO> listarCertificados() {
        List<Certificado> listado = _certificadoRepository.findAll();
        return listado.stream().map(this::mapearCertificadoDTO).toList();
    }

    @Override
    public CertificadoDTO actualizarCertificado(CertificadoDTO certificadoDTO) {
        Certificado certificado = _certificadoRepository.findById(certificadoDTO.getIdCertificado())
                .orElseThrow(() -> new RecursoNoEncontradoException("Certificado no encontrado"));

        certificado.setTipoCertificado(certificadoDTO.getTipoCertificado());

        if (certificadoDTO.getIdUsuario() != null) {
            Usuario usuario = _usuarioRepository.findById(certificadoDTO.getIdUsuario())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
            certificado.setUsuario(usuario);
        }

        if (certificadoDTO.getIdEvento() != null) {
            Evento evento = _eventoRepository.findById(certificadoDTO.getIdEvento())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Evento no encontrado"));
            certificado.setEvento(evento);
        }

        certificado = _certificadoRepository.save(certificado);
        return mapearCertificadoDTO(certificado);
    }

    @Override
    public String inactivarCertificado(Long idCertificado) {
        throw new OperacionNoPermitidaException(
                "Borrado físico prohibido. Los certificados emitidos son inmutables.");
    }

    @Override
    public boolean borrarCertificado(Long idCertificado) {
        throw new OperacionNoPermitidaException(
                "Borrado físico prohibido. Los certificados emitidos son inmutables.");
    }

    @Override
    @Transactional
    public CertificadoDTO emitirCertificadoPorAsistencia(Long idInscripcion) {
        Inscripcion ins = _inscripcionRepository.findById(idInscripcion)
                .orElseThrow(() -> new RecursoNoEncontradoException("Inscripción no encontrada"));

        if (!com.certiva.api.Util.InscripcionEstadoHelper.tieneAsistenciaConfirmada(ins.getEstado())) {
            throw new OperacionNoPermitidaException("La inscripción no tiene asistencia confirmada.");
        }
        com.certiva.api.enums.EstadoOperativoEvento op = com.certiva.api.Util.EstadoOperativoEventoHelper
                .resolverOperativoVisible(ins.getEvento(), java.time.LocalDateTime.now());
        if (op != com.certiva.api.enums.EstadoOperativoEvento.CERRADO_Y_CERTIFICADO
                && op != com.certiva.api.enums.EstadoOperativoEvento.EN_REVISION) {
            throw new OperacionNoPermitidaException(
                    "Los certificados solo se emiten durante la revisión o el cierre del evento.");
        }

        Optional<Certificado> existente = _certificadoRepository.findFirstByUsuario_IdUsuarioAndEvento_IdEvento(
                ins.getUsuario().getIdUsuario(), ins.getEvento().getIdEvento());
        if (existente.isPresent()) {
            return mapearCertificadoDTO(existente.get());
        }

        _elegibilidadService.validarElegibilidad(ins);

        String codigo = generarCodigoUnico();
        String urlVerificacion = _appProperties.urlVerificarCertificado(codigo);
        byte[] pdf = _pdfCertificadoGenerator.generarPdf(
                ins.getEvento().getNombreEvento(),
                ins.getUsuario().getNombres() + " " + ins.getUsuario().getApellidos(),
                codigo,
                urlVerificacion);

        Certificado certificado = new Certificado();
        certificado.setTipoCertificado("ASISTENCIA");
        certificado.setCodigoValidacion(codigo);
        certificado.setFechaEmision(LocalDateTime.now());
        certificado.setEstado(EstadoCertificado.VALIDO);
        certificado.setUsuario(ins.getUsuario());
        certificado.setEvento(ins.getEvento());
        certificado.setContenidoPdf(pdf);

        certificado = _certificadoRepository.save(certificado);
        return mapearCertificadoDTO(certificado);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CertificadoDTO> buscarPorUsuarioYEvento(Long idUsuario, Long idEvento) {
        return _certificadoRepository.findFirstByUsuario_IdUsuarioAndEvento_IdEvento(idUsuario, idEvento)
                .map(this::mapearCertificadoDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public CertificadoVerificacionDTO verificarPublicoPorCodigo(String codigoValidacion) {
        if (codigoValidacion == null || codigoValidacion.isBlank()) {
            return CertificadoVerificacionDTO.builder()
                    .valido(false)
                    .mensaje("Código vacío")
                    .build();
        }
        Optional<Certificado> opt = buscarPorCodigoFlexible(codigoValidacion);
        if (opt.isEmpty()) {
            return CertificadoVerificacionDTO.builder()
                    .valido(false)
                    .mensaje("Certificado no encontrado")
                    .build();
        }
        Certificado c = opt.get();
        if (c.getEstado() == EstadoCertificado.REVOCADO) {
            return CertificadoVerificacionDTO.builder()
                    .valido(false)
                    .mensaje("Certificado revocado")
                    .codigoValidacion(c.getCodigoValidacion())
                    .build();
        }
        Usuario u = c.getUsuario();
        Evento e = c.getEvento();
        return CertificadoVerificacionDTO.builder()
                .valido(true)
                .mensaje("Certificado válido")
                .codigoValidacion(c.getCodigoValidacion())
                .nombreParticipante(u.getNombres() + " " + u.getApellidos())
                .tituloEvento(e.getNombreEvento())
                .fechaEmision(c.getFechaEmision())
                .build();
    }

    private CertificadoDTO mapearCertificadoDTO(Certificado c) {
        CertificadoDTO dto = _modelMapper.map(c, CertificadoDTO.class);
        dto.setIdUsuario(c.getUsuario().getIdUsuario());
        dto.setIdEvento(c.getEvento().getIdEvento());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertificadoPortalDTO> listarMisCertificados() {
        Usuario usuario = _securityUsuarioHelper.usuarioAutenticado();
        return _certificadoRepository.findByUsuario_IdUsuarioOrderByFechaEmisionDesc(usuario.getIdUsuario())
                .stream()
                .map(this::mapearCertificadoPortal)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] descargarPdfMiCertificado(Long idCertificado) {
        Usuario usuario = _securityUsuarioHelper.usuarioAutenticado();
        Certificado certificado = _certificadoRepository.findById(idCertificado)
                .orElseThrow(() -> new RecursoNoEncontradoException("Certificado no encontrado"));
        if (!certificado.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
            throw new OperacionNoPermitidaException("No puede descargar certificados de otro usuario.");
        }
        if (certificado.getContenidoPdf() == null || certificado.getContenidoPdf().length == 0) {
            return obtenerPdfCertificado(certificado);
        }
        return certificado.getContenidoPdf();
    }

    @Override
    @Transactional
    public CertificadoDTO emitirCertificadoMiInscripcion(Long idInscripcion) {
        Usuario usuario = _securityUsuarioHelper.usuarioAutenticado();
        Inscripcion inscripcion = _inscripcionRepository.findById(idInscripcion)
                .orElseThrow(() -> new RecursoNoEncontradoException("Inscripción no encontrada"));
        if (!inscripcion.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
            throw new OperacionNoPermitidaException("La inscripción no pertenece a su cuenta.");
        }
        return emitirCertificadoPorAsistencia(idInscripcion);
    }

    @Override
    @Transactional(readOnly = true)
    public CertificadosAdminVistaDTO obtenerVistaAdmin(String busqueda, Long idEvento) {
        String q = normalizarBusqueda(busqueda);
        List<Certificado> list = _certificadoRepository.buscarVistaAdmin(q, idEvento);

        long totalEmitidos = _certificadoRepository.count();
        long eventosClausurados = _eventoRepository.countByEstadoOperativo(EstadoOperativoEvento.CERRADO_Y_CERTIFICADO);
        long totalInscripciones = _inscripcionRepository.count();
        long certificadosValidos = _certificadoRepository.countByEstado(EstadoCertificado.VALIDO);
        double tasaAprobacion = totalInscripciones > 0
                ? Math.round(certificadosValidos * 1000.0 / totalInscripciones) / 10.0
                : 0.0;

        Optional<Certificado> ultimo = _certificadoRepository.findTopByEstadoOrderByFechaEmisionDesc(EstadoCertificado.VALIDO);
        String ultimaEmisionTexto = "Sin emisiones registradas";
        String ultimoEventoNombre = null;
        if (ultimo.isPresent()) {
            Certificado u = ultimo.get();
            ultimoEventoNombre = u.getEvento().getNombreEvento();
            ultimaEmisionTexto = formatearTiempoRelativo(u.getFechaEmision());
        }

        CertificadosAdminKpiDTO kpis = CertificadosAdminKpiDTO.builder()
                .totalEmitidos(totalEmitidos)
                .eventosClausurados(eventosClausurados)
                .tasaAprobacion(tasaAprobacion)
                .ultimaEmisionTexto(ultimaEmisionTexto)
                .ultimoEventoNombre(ultimoEventoNombre)
                .build();

        List<CertificadoAdminFilaDTO> filas = list.stream().map(this::mapearAdminFila).toList();
        List<EventoOpcionFiltroDTO> eventosFiltro = _certificadoRepository.listarEventosConCertificados().stream()
                .map(row -> EventoOpcionFiltroDTO.builder()
                        .idEvento((Long) row[0])
                        .nombreEvento((String) row[1])
                        .build())
                .toList();

        return CertificadosAdminVistaDTO.builder()
                .kpis(kpis)
                .certificados(filas)
                .eventosFiltro(eventosFiltro)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] descargarPdfAdmin(Long idCertificado) {
        Certificado certificado = _certificadoRepository.findById(idCertificado)
                .orElseThrow(() -> new RecursoNoEncontradoException("Certificado no encontrado"));
        return obtenerPdfCertificado(certificado);
    }

    @Override
    @Transactional
    public CertificadoAdminFilaDTO revocarCertificado(Long idCertificado) {
        Certificado certificado = _certificadoRepository.findById(idCertificado)
                .orElseThrow(() -> new RecursoNoEncontradoException("Certificado no encontrado"));
        if (certificado.getEstado() == EstadoCertificado.REVOCADO) {
            throw new OperacionNoPermitidaException("El certificado ya está revocado.");
        }
        certificado.setEstado(EstadoCertificado.REVOCADO);
        certificado = _certificadoRepository.save(certificado);
        Usuario admin = _securityUsuarioHelper.usuarioAutenticado();
        _auditoriaService.registrarAuditoriaHttp(
                "CERTIFICATE_REVOKED",
                "Certificado",
                "Revocado certificado " + certificado.getCodigoValidacion(),
                null,
                admin);
        return mapearAdminFila(certificado);
    }

    private byte[] obtenerPdfCertificado(Certificado certificado) {
        if (certificado.getContenidoPdf() != null && certificado.getContenidoPdf().length > 0) {
            return certificado.getContenidoPdf();
        }
        Usuario usuario = certificado.getUsuario();
        String urlVerificacion = _appProperties.urlVerificarCertificado(certificado.getCodigoValidacion());
        byte[] pdf = _pdfCertificadoGenerator.generarPdf(
                certificado.getEvento().getNombreEvento(),
                usuario.getNombres() + " " + usuario.getApellidos(),
                certificado.getCodigoValidacion(),
                urlVerificacion);
        certificado.setContenidoPdf(pdf);
        _certificadoRepository.save(certificado);
        return pdf;
    }

    private CertificadoAdminFilaDTO mapearAdminFila(Certificado c) {
        Usuario u = c.getUsuario();
        boolean tienePdf = c.getContenidoPdf() != null && c.getContenidoPdf().length > 0;
        return CertificadoAdminFilaDTO.builder()
                .idCertificado(c.getIdCertificado())
                .codigoValidacion(c.getCodigoValidacion())
                .codigoMostrar(formatearCodigoMostrar(c.getCodigoValidacion()))
                .nombreParticipante(u.getNombres() + " " + u.getApellidos())
                .numeroDocumento(u.getNumeroDocumento())
                .idEvento(c.getEvento().getIdEvento())
                .nombreEvento(c.getEvento().getNombreEvento())
                .fechaEmision(c.getFechaEmision())
                .estado(c.getEstado())
                .tienePdf(tienePdf)
                .build();
    }

    private static String formatearCodigoMostrar(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return "";
        }
        String c = codigo.replace("-", "").toUpperCase();
        if (c.length() < 8) {
            return "CERT-" + c;
        }
        return "CERT-" + c.substring(0, 4) + "-" + c.substring(4, 8);
    }

    private static String normalizarBusqueda(String busqueda) {
        if (busqueda == null || busqueda.isBlank()) {
            return null;
        }
        String b = busqueda.trim();
        if (b.toUpperCase().startsWith("CERT-")) {
            b = b.substring(5);
        }
        return b.replace("-", "");
    }

    private Optional<Certificado> buscarPorCodigoFlexible(String codigoValidacion) {
        if (codigoValidacion == null || codigoValidacion.isBlank()) {
            return Optional.empty();
        }
        String raw = codigoValidacion.trim();
        Optional<Certificado> directo = _certificadoRepository.findByCodigoValidacion(raw);
        if (directo.isPresent()) {
            return directo;
        }
        String normalizado = normalizarBusqueda(raw);
        if (normalizado == null || normalizado.equals(raw)) {
            return Optional.empty();
        }
        return _certificadoRepository.findByCodigoValidacion(normalizado);
    }

    private static String formatearTiempoRelativo(LocalDateTime fecha) {
        if (fecha == null) {
            return "Sin fecha";
        }
        Duration d = Duration.between(fecha, LocalDateTime.now());
        long minutos = d.toMinutes();
        if (minutos < 1) {
            return "Hace un momento";
        }
        if (minutos < 60) {
            return "Hace " + minutos + " min";
        }
        long horas = d.toHours();
        if (horas < 24) {
            return "Hace " + horas + " h";
        }
        long dias = d.toDays();
        if (dias < 30) {
            return "Hace " + dias + " d";
        }
        return fecha.toLocalDate().toString();
    }

    private CertificadoPortalDTO mapearCertificadoPortal(Certificado c) {
        boolean tienePdf = c.getContenidoPdf() != null && c.getContenidoPdf().length > 0;
        return CertificadoPortalDTO.builder()
                .idCertificado(c.getIdCertificado())
                .codigoValidacion(c.getCodigoValidacion())
                .nombreEvento(c.getEvento().getNombreEvento())
                .tipoEvento(c.getEvento().getTipoEvento() != null ? c.getEvento().getTipoEvento().name() : null)
                .fechaEmision(c.getFechaEmision())
                .puedeDescargar(tienePdf)
                .motivoPendiente(tienePdf ? null : "Generando documento…")
                .build();
    }

    private String generarCodigoUnico() {
        for (int i = 0; i < 12; i++) {
            String codigo = UUID.randomUUID().toString().replace("-", "");
            if (!_certificadoRepository.existsByCodigoValidacion(codigo)) {
                return codigo;
            }
        }
        throw new IllegalStateException("No se pudo generar un código de validación único");
    }
}
