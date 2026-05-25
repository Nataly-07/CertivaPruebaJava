package com.certiva.api.Impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.certiva.api.DTO.CampoFormularioDTO;
import com.certiva.api.DTO.CrearCampoFormularioDTO;
import com.certiva.api.DTO.CrearEventoDTO;
import com.certiva.api.DTO.DetalleCursoDTO;
import com.certiva.api.DTO.DetalleFeriaDTO;
import com.certiva.api.DTO.DetalleHackathonDTO;
import com.certiva.api.DTO.DetalleTallerDTO;
import com.certiva.api.Config.CertivaAppProperties;
import com.certiva.api.DTO.EventoCatalogoPublicoDTO;
import com.certiva.api.DTO.EventoCupoVerificacionDTO;
import com.certiva.api.DTO.EventoDTO;
import com.certiva.api.DTO.EventoFilaAdminDTO;
import com.certiva.api.DTO.EventoPublicoDTO;
import com.certiva.api.DTO.EventoResumenTipoDTO;
import com.certiva.api.DTO.EventoRevisionAlumnoDTO;
import com.certiva.api.DTO.EventoRevisionPanelDTO;
import com.certiva.api.DTO.ProfesorEventoTarjetaDTO;
import com.certiva.api.DTO.ProfesorPanelBannerDTO;
import com.certiva.api.DTO.ProfesorPanelDTO;
import com.certiva.api.DTO.ProfesorPanelDTO.ProfesorEventoResumenDTO;
import com.certiva.api.Service.CertificadoElegibilidadService;
import com.certiva.api.Util.ProfesorAsistenciaHelper;
import com.certiva.api.Entity.CampoFormulario;
import com.certiva.api.Entity.CursoEvento;
import com.certiva.api.Entity.Evento;
import com.certiva.api.Entity.FeriaProyectoEvento;
import com.certiva.api.Entity.HackathonEvento;
import com.certiva.api.Entity.TallerEvento;
import com.certiva.api.Entity.Usuario;
import com.certiva.api.Exception.OperacionNoPermitidaException;
import com.certiva.api.Exception.RecursoNoEncontradoException;
import com.certiva.api.Repository.CampoFormularioRepository;
import com.certiva.api.Repository.EventoListadoJdbcRepository;
import com.certiva.api.Repository.EventoListadoJdbcRepository.CatalogoPublicoRow;
import com.certiva.api.Repository.EventoListadoJdbcRepository.EventoListadoRow;
import com.certiva.api.Repository.EventoRepository;
import com.certiva.api.Repository.InscripcionRepository;
import com.certiva.api.Repository.ResultadoEvaluacionRepository;
import com.certiva.api.Repository.UsuarioRepository;
import com.certiva.api.Entity.Inscripcion;
import com.certiva.api.Service.AuditoriaService;
import com.certiva.api.Service.EventoService;
import com.certiva.api.Util.EventoArchivoStorage;
import com.certiva.api.Util.EventoEdicionPolicy;
import com.certiva.api.Util.EventoMapper;
import com.certiva.api.Util.EstadoOperativoEventoHelper;
import com.certiva.api.Util.InscripcionEstadoHelper;
import com.certiva.api.Util.SecurityUsuarioHelper;
import com.certiva.api.enums.AuditoriaAccion;
import com.certiva.api.enums.EstadoOperativoEvento;
import com.certiva.api.Service.EventoCicloVidaService;
import com.certiva.api.Util.TipoEventoCatalogoHelper;
import com.certiva.api.enums.ModalidadEvento;
import com.certiva.api.enums.TipoDatoCampo;
import com.certiva.api.enums.TipoEventoEnum;

@Service
public class EventoServiceImpl implements EventoService {

    private static final Logger log = LoggerFactory.getLogger(EventoServiceImpl.class);

    private static final String ROL_PROFESOR = "ROLE_PROFESOR";
    private static final String ROL_MONITOR = "ROLE_MONITOR";

    private final EventoRepository _eventoRepository;
    private final EventoListadoJdbcRepository _eventoListadoJdbc;
    private final TipoEventoCatalogoHelper _catalogoHelper;
    private final UsuarioRepository _usuarioRepository;
    private final InscripcionRepository _inscripcionRepository;
    private final CampoFormularioRepository _campoFormularioRepository;
    private final AuditoriaService _auditoriaService;
    private final EventoArchivoStorage _archivoStorage;
    private final EventoMapper _eventoMapper;
    private final ObjectMapper _objectMapper;
    private final CertivaAppProperties _appProperties;
    private final SecurityUsuarioHelper _securityUsuarioHelper;
    private final EventoCicloVidaService _eventoCicloVidaService;
    private final CertificadoElegibilidadService _elegibilidadService;
    private final ResultadoEvaluacionRepository _resultadoEvaluacionRepository;

    public EventoServiceImpl(EventoRepository eventoRepository,
                             EventoListadoJdbcRepository eventoListadoJdbc,
                             TipoEventoCatalogoHelper catalogoHelper,
                             UsuarioRepository usuarioRepository,
                             InscripcionRepository inscripcionRepository,
                             CampoFormularioRepository campoFormularioRepository,
                             AuditoriaService auditoriaService,
                             EventoArchivoStorage archivoStorage,
                             EventoMapper eventoMapper,
                             ObjectMapper objectMapper,
                             CertivaAppProperties appProperties,
                             SecurityUsuarioHelper securityUsuarioHelper,
                             EventoCicloVidaService eventoCicloVidaService,
                             CertificadoElegibilidadService elegibilidadService,
                             ResultadoEvaluacionRepository resultadoEvaluacionRepository) {
        this._eventoRepository = eventoRepository;
        this._eventoListadoJdbc = eventoListadoJdbc;
        this._catalogoHelper = catalogoHelper;
        this._usuarioRepository = usuarioRepository;
        this._inscripcionRepository = inscripcionRepository;
        this._campoFormularioRepository = campoFormularioRepository;
        this._auditoriaService = auditoriaService;
        this._archivoStorage = archivoStorage;
        this._eventoMapper = eventoMapper;
        this._objectMapper = objectMapper;
        this._appProperties = appProperties;
        this._securityUsuarioHelper = securityUsuarioHelper;
        this._eventoCicloVidaService = eventoCicloVidaService;
        this._elegibilidadService = elegibilidadService;
        this._resultadoEvaluacionRepository = resultadoEvaluacionRepository;
    }

    @Override
    @Transactional
    public EventoDTO crearEvento(CrearEventoDTO dto, MultipartFile imagen, MultipartFile pensum) {
        asegurarRolCrearEditar();
        validarFechas(dto.getFechaInicio(), dto.getFechaFin(), true);
        validarDetallePorTipo(dto);
        normalizarStaffEnDto(dto);

        Usuario creador = usuarioActualDesdeSeguridad();
        Evento evento = nuevaInstancia(dto.getTipoEvento());
        aplicarCamposComunes(evento, dto);
        evento.setEstado(true);
        evento.setEstadoOperativo(EstadoOperativoEvento.PROXIMO);
        evento.setUsuarioCreador(creador);
        aplicarDetalleEnEntidad(evento, dto);
        _catalogoHelper.asignarCatalogoSiFalta(evento);
        validarEntidadAntesDeGuardar(evento);
        asegurarCodigoDifusionUnico(evento);

        if (imagen != null && !imagen.isEmpty()) {
            evento.setRutaImagenPromocional(_archivoStorage.guardarImagen(imagen));
        }
        if (pensum != null && !pensum.isEmpty()) {
            evento.setRutaPensum(_archivoStorage.guardarPensum(pensum));
        }

        Set<Usuario> profesores = cargarUsuariosPorRolExclusivo(
                dto.getIdsProfesoresColaboradores(), ROL_PROFESOR);
        Set<Usuario> monitores = cargarUsuariosPorRolExclusivo(
                dto.getIdsMonitoresAsignados(), ROL_MONITOR);
        evento.setProfesoresColaboradores(profesores);
        evento.setMonitoresAsignados(monitores);

        evento = _eventoRepository.save(evento);
        persistirCamposDesdeCrear(evento, dto.getCamposPersonalizados());

        _auditoriaService.registrarAuditoria(
                AuditoriaAccion.EVENT_CREATED,
                "El usuario " + creador.getIdUsuario() + " creó el evento \"" + evento.getNombreEvento() + "\"",
                null,
                creador);

        return construirDtoPostGuardado(evento, profesores, monitores, creador);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventoDTO> listarEventos(Boolean soloActivos,
                                         ModalidadEvento modalidad,
                                         TipoEventoEnum tipo,
                                         LocalDateTime desde,
                                         LocalDateTime hasta) {
        Boolean activo = resolverFiltroActivo(soloActivos);
        return _eventoRepository.buscarConFiltros(activo, modalidad, tipo, desde, hasta).stream()
                .map(e -> _eventoMapper.toDto(e, false))
                .toList();
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED, readOnly = true)
    public List<EventoResumenTipoDTO> listarResumenTipos(Boolean soloActivos, ModalidadEvento modalidad) {
        Boolean activo = resolverFiltroActivo(soloActivos);
        List<EventoListadoRow> filas = listarFilasJdbc(activo, modalidad, null, null, null);
        Map<Long, Long> inscritos = mapaInscritosPorFilas(filas);
        Map<TipoEventoEnum, List<EventoListadoRow>> porTipo = filas.stream()
                .collect(Collectors.groupingBy(EventoListadoRow::tipoEvento));

        List<EventoResumenTipoDTO> salida = new ArrayList<>();
        for (TipoEventoEnum tipo : TipoEventoEnum.values()) {
            List<EventoListadoRow> lista = porTipo.getOrDefault(tipo, List.of());
            long totalInscritos = 0;
            long totalAforo = 0;
            for (EventoListadoRow e : lista) {
                totalInscritos += inscritos.getOrDefault(e.idEvento(), 0L);
                if (e.aforoMaximo() != null) {
                    totalAforo += e.aforoMaximo();
                }
            }
            double pct = totalAforo > 0 ? (totalInscritos * 100.0) / totalAforo : 0.0;
            salida.add(new EventoResumenTipoDTO(
                    tipo,
                    lista.size(),
                    Math.round(pct * 10.0) / 10.0));
        }
        return salida;
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED, readOnly = true)
    public List<EventoFilaAdminDTO> listarVistaAdmin(Boolean soloActivos,
                                                    ModalidadEvento modalidad,
                                                    TipoEventoEnum tipo,
                                                    LocalDateTime desde,
                                                    LocalDateTime hasta) {
        Boolean activo = resolverFiltroActivo(soloActivos);
        List<EventoListadoRow> filas = listarFilasJdbc(activo, modalidad, tipo, desde, hasta);
        Map<Long, Long> inscritos = mapaInscritosPorFilas(filas);
        return filas.stream()
                .map(row -> toFilaAdmin(row, inscritos.getOrDefault(row.idEvento(), 0L)))
                .toList();
    }

    private List<EventoListadoRow> listarFilasJdbc(
            Boolean activo,
            ModalidadEvento modalidad,
            TipoEventoEnum tipo,
            LocalDateTime desde,
            LocalDateTime hasta) {
        return _eventoListadoJdbc.listarConFiltros(activo, modalidad, tipo, desde, hasta);
    }

    private Map<Long, Long> mapaInscritosPorFilas(List<EventoListadoRow> filas) {
        List<Long> ids = filas.stream()
                .map(EventoListadoRow::idEvento)
                .filter(Objects::nonNull)
                .toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        try {
            return _eventoListadoJdbc.contarInscritosActivosPorEvento(ids);
        } catch (Exception ex) {
            log.warn("No se pudo contar inscripciones por evento; se asume 0: {}", ex.getMessage());
            return Map.of();
        }
    }

    private EventoFilaAdminDTO toFilaAdmin(EventoListadoRow row, long inscritosActivos) {
        EventoFilaAdminDTO dto = new EventoFilaAdminDTO();
        dto.setIdEvento(row.idEvento());
        dto.setNombreEvento(row.nombreEvento());
        dto.setTipoEvento(row.tipoEvento());
        dto.setModalidad(row.modalidad());
        dto.setInstructorPrincipal(resolverInstructorDesdeFila(row));
        dto.setInscritosActivos(inscritosActivos);
        dto.setAforoMaximo(row.aforoMaximo());
        dto.setEstado(row.estado());
        if (Boolean.FALSE.equals(row.estado())) {
            dto.setEstadoOperativo(EstadoOperativoEvento.EVENT_CANCELLED);
        } else {
            dto.setEstadoOperativo(EstadoOperativoEventoHelper.calcularPorReloj(
                    row.fechaInicio(), row.fechaFin(), LocalDateTime.now()));
        }
        dto.setFechaInicio(row.fechaInicio());
        dto.setFechaFin(row.fechaFin());
        return dto;
    }

    private void aplicarSoloPersonal(Evento evento, EventoDTO dto) {
        if (dto.getIdsProfesoresColaboradores() != null || dto.getProfesoresColaboradores() != null) {
            List<Long> idsProf = dto.getIdsProfesoresColaboradores() != null
                    ? dto.getIdsProfesoresColaboradores()
                    : dto.getProfesoresColaboradores().stream().map(p -> p.getIdUsuario()).toList();
            evento.setProfesoresColaboradores(cargarUsuariosPorRolExclusivo(idsProf, ROL_PROFESOR));
        }
        if (dto.getIdsMonitoresAsignados() != null || dto.getMonitoresAsignados() != null) {
            List<Long> idsMon = dto.getIdsMonitoresAsignados() != null
                    ? dto.getIdsMonitoresAsignados()
                    : dto.getMonitoresAsignados().stream().map(m -> m.getIdUsuario()).toList();
            evento.setMonitoresAsignados(cargarUsuariosPorRolExclusivo(idsMon, ROL_MONITOR));
        }
    }

    private static String resolverInstructorDesdeFila(EventoListadoRow row) {
        String nombres = row.creadorNombres() != null ? row.creadorNombres().trim() : "";
        String apellidos = row.creadorApellidos() != null ? row.creadorApellidos().trim() : "";
        String completo = (nombres + " " + apellidos).trim();
        return completo.isEmpty() ? "Sin asignar" : completo;
    }

    @Override
    @Transactional
    public EventoDTO obtenerPorId(Long idEvento) {
        Evento evento = cargarEventoConStaff(idEvento);
        asegurarCodigoDifusion(evento);
        EventoDTO dto = _eventoMapper.toDto(evento, true);
        dto.setCamposPersonalizados(camposADto(idEvento));
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampoFormularioDTO> listarCamposPorEvento(Long idEvento) {
        if (!_eventoRepository.existsById(idEvento)) {
            throw new RecursoNoEncontradoException("Evento no encontrado");
        }
        return camposADto(idEvento);
    }

    @Override
    @Transactional
    public EventoDTO actualizarEvento(EventoDTO dto) {
        asegurarRolCrearEditar();
        if (dto.getIdEvento() == null) {
            throw new OperacionNoPermitidaException("El id del evento es obligatorio para actualizar.");
        }
        validarFechas(dto.getFechaInicio(), dto.getFechaFin(), false);
        normalizarStaffEnEventoDto(dto);

        Evento evento = cargarEventoConStaff(dto.getIdEvento());
        EstadoOperativoEventoHelper.sincronizarAutomatico(evento, LocalDateTime.now());

        if (!evento.getTipoEvento().equals(dto.getTipoEvento())) {
            throw new OperacionNoPermitidaException("No se permite cambiar el tipo de evento tras su creación.");
        }

        Usuario editor = usuarioActualDesdeSeguridad();
        boolean esAdmin = EventoEdicionPolicy.esAdminAutenticado();
        EventoEdicionPolicy.validarEdicion(evento, dto, esAdmin);

        if (esAdmin && evento.getEstadoOperativo() == EstadoOperativoEvento.EN_CURSO) {
            aplicarSoloPersonal(evento, dto);
            evento = _eventoRepository.save(evento);
            _auditoriaService.registrarAuditoria(
                    "EVENTO_PERSONAL_ACTUALIZAR",
                    "Reasignación de personal en evento en curso id=" + evento.getIdEvento(),
                    null,
                    editor);
            EventoDTO salidaStaff = _eventoMapper.toDto(evento, true);
            salidaStaff.setCamposPersonalizados(camposADto(evento.getIdEvento()));
            return salidaStaff;
        }

        evento.setNombreEvento(dto.getNombreEvento());
        evento.setDescripcion(dto.getDescripcion());
        evento.setModalidad(dto.getModalidad());
        evento.setFechaInicio(dto.getFechaInicio());
        evento.setFechaFin(dto.getFechaFin());
        evento.setUbicacion(dto.getUbicacion());
        evento.setEnlaceVirtual(dto.getEnlaceVirtual());
        evento.setAforoMaximo(dto.getAforoMaximo());
        evento.setIntensidadHoraria(dto.getIntensidadHoraria());
        evento.setCosto(dto.getPrecio() != null ? dto.getPrecio() : 0.0);
        evento.setTextoDiploma(dto.getTextoDiploma());
        evento.setFirmaDigitalProfesor(dto.getFirmaDigitalProfesor());
        if (dto.getEstado() != null) {
            evento.setEstado(dto.getEstado());
        }

        aplicarDetalleEnEntidadDesdeDto(evento, dto);

        if (dto.getIdsProfesoresColaboradores() != null || dto.getProfesoresColaboradores() != null) {
            List<Long> idsProf = dto.getIdsProfesoresColaboradores() != null
                    ? dto.getIdsProfesoresColaboradores()
                    : dto.getProfesoresColaboradores().stream().map(p -> p.getIdUsuario()).toList();
            evento.setProfesoresColaboradores(cargarUsuariosPorRolExclusivo(idsProf, ROL_PROFESOR));
        }
        if (dto.getIdsMonitoresAsignados() != null || dto.getMonitoresAsignados() != null) {
            List<Long> idsMon = dto.getIdsMonitoresAsignados() != null
                    ? dto.getIdsMonitoresAsignados()
                    : dto.getMonitoresAsignados().stream().map(m -> m.getIdUsuario()).toList();
            evento.setMonitoresAsignados(cargarUsuariosPorRolExclusivo(idsMon, ROL_MONITOR));
        }

        if (dto.getCamposPersonalizados() != null) {
            long ocupados = _inscripcionRepository.countCuposOcupadosPorEvento(evento.getIdEvento());
            if (ocupados == 0) {
                _campoFormularioRepository.deleteByEvento_IdEvento(evento.getIdEvento());
                persistirCamposDesdeCrear(evento, mapDtoACrear(dto.getCamposPersonalizados()));
            }
        }

        evento = _eventoRepository.save(evento);

        _auditoriaService.registrarAuditoria(
                "EVENTO_ACTUALIZAR",
                "El usuario " + editor.getIdUsuario() + " modificó el evento \"" + evento.getNombreEvento() + "\" (id "
                        + evento.getIdEvento() + ")",
                null,
                editor);

        EventoDTO salida = _eventoMapper.toDto(evento, true);
        salida.setCamposPersonalizados(camposADto(evento.getIdEvento()));
        return salida;
    }

    @Override
    @Transactional
    public String inactivarEvento(Long idEvento) {
        cancelarEvento(idEvento);
        return "Evento cancelado correctamente";
    }

    @Override
    @Transactional
    public void cancelarEvento(Long idEvento) {
        _eventoCicloVidaService.cancelarEvento(idEvento);
    }

    @Override
    @Transactional
    public void borrarEventoLogico(Long idEvento) {
        cancelarEvento(idEvento);
    }

    @Override
    @Transactional(readOnly = true)
    public EventoPublicoDTO obtenerPublicoPorCodigoDifusion(String codigoDifusion) {
        if (codigoDifusion == null || codigoDifusion.isBlank()) {
            throw new RecursoNoEncontradoException("Código de difusión inválido");
        }
        Evento evento = _eventoRepository.findByCodigoDifusionAndEstadoTrue(codigoDifusion.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento no encontrado o inactivo"));
        EstadoOperativoEvento op = EstadoOperativoEventoHelper.resolverOperativoVisible(evento, LocalDateTime.now());
        if (op != EstadoOperativoEvento.PROXIMO) {
            throw new OperacionNoPermitidaException(
                    "El evento ya no admite inscripciones (estado: " + op + ").");
        }
        EventoCupoVerificacionDTO cupo = verificarCupo(evento.getIdEvento());
        return EventoPublicoDTO.builder()
                .idEvento(evento.getIdEvento())
                .nombreEvento(evento.getNombreEvento())
                .descripcion(evento.getDescripcion())
                .tipoEvento(evento.getTipoEvento())
                .modalidad(evento.getModalidad())
                .fechaInicio(evento.getFechaInicio())
                .fechaFin(evento.getFechaFin())
                .ubicacion(evento.getUbicacion())
                .enlaceVirtual(evento.getEnlaceVirtual())
                .aforoMaximo(evento.getAforoMaximo())
                .intensidadHoraria(evento.getIntensidadHoraria())
                .precio(evento.getCosto())
                .gratuito(evento.isGratuito())
                .rutaImagenPromocional(evento.getRutaImagenPromocional())
                .codigoDifusion(evento.getCodigoDifusion())
                .urlInscripcionPublica(_appProperties.urlInscripcionPorCodigoDifusion(evento.getCodigoDifusion()))
                .hayCupoDisponible(cupo.isHayCupoDisponible())
                .camposPersonalizados(camposADto(evento.getIdEvento()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventoCatalogoPublicoDTO> listarCatalogoPublico() {
        List<CatalogoPublicoRow> filas = _eventoListadoJdbc.listarCatalogoPublico();
        return mapearFilasCatalogo(filas);
    }

    private List<EventoCatalogoPublicoDTO> mapearFilasCatalogo(List<CatalogoPublicoRow> filas) {
        Map<Long, Long> inscritos = mapaInscritosPorFilasCatalogo(filas);
        return filas.stream()
                .map(row -> toCatalogoPublicoDto(row, inscritos.getOrDefault(row.idEvento(), 0L)))
                .toList();
    }

    private Map<Long, Long> mapaInscritosPorFilasCatalogo(List<CatalogoPublicoRow> filas) {
        List<Long> ids = filas.stream().map(CatalogoPublicoRow::idEvento).filter(Objects::nonNull).toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        try {
            return _eventoListadoJdbc.contarInscritosActivosPorEvento(ids);
        } catch (Exception ex) {
            log.warn("Conteo inscripciones catálogo: {}", ex.getMessage());
            return Map.of();
        }
    }

    private EventoCatalogoPublicoDTO toCatalogoPublicoDto(CatalogoPublicoRow row, long inscritos) {
        int max = row.aforoMaximo() != null ? row.aforoMaximo() : 0;
        boolean hayCupo = max <= 0 || inscritos < max;
        double precio = row.precio() != null ? row.precio() : 0.0;
        String nombre = row.nombreEvento() != null ? row.nombreEvento() : "Evento sin título";
        return EventoCatalogoPublicoDTO.builder()
                .idEvento(row.idEvento())
                .nombreEvento(nombre)
                .descripcion(row.descripcion())
                .tipoEvento(row.tipoEvento())
                .area(areaPorTipo(row.tipoEvento()))
                .modalidad(row.modalidad())
                .fechaInicio(row.fechaInicio())
                .fechaFin(row.fechaFin())
                .ubicacion(row.ubicacion())
                .enlaceVirtual(row.enlaceVirtual())
                .aforoMaximo(max > 0 ? max : null)
                .precio(precio)
                .gratuito(precio <= 0.0)
                .rutaImagenPromocional(row.rutaImagenPromocional())
                .instructorNombres(row.creadorNombres())
                .instructorApellidos(row.creadorApellidos())
                .instructorRolEtiqueta("Profesor líder")
                .inscritosActivos(inscritos)
                .hayCupoDisponible(hayCupo)
                .build();
    }

    private static String areaPorTipo(TipoEventoEnum tipo) {
        if (tipo == null) {
            return "TECNOLOGÍA";
        }
        return switch (tipo) {
            case CURSO -> "TECNOLOGÍA";
            case HACKATHON -> "DESARROLLO";
            case FERIA -> "INNOVACIÓN";
            case TALLER -> "PRÁCTICO";
        };
    }

    @Override
    @Transactional(readOnly = true)
    public ProfesorPanelDTO obtenerPanelProfesor() {
        asegurarRolCrearEditar();
        Usuario profesor = _securityUsuarioHelper.usuarioAutenticado();
        LocalDateTime ahora = LocalDateTime.now();
        LocalDate hoy = ahora.toLocalDate();
        List<Evento> eventos = listarEventosGestionadosPorProfesor(profesor.getIdUsuario());

        long totalInscritos = 0;
        long activos = 0;
        long eventosPorCertificar = 0;
        List<ProfesorEventoResumenDTO> resumenes = new ArrayList<>();
        List<ProfesorEventoTarjetaDTO> enCurso = new ArrayList<>();
        List<ProfesorEventoTarjetaDTO> pendientesCierre = new ArrayList<>();
        List<ProfesorEventoTarjetaDTO> historial = new ArrayList<>();
        ProfesorPanelBannerDTO banner = ProfesorPanelBannerDTO.builder().sesionActivaHoy(false).build();

        for (Evento e : eventos) {
            EstadoOperativoEvento op = EstadoOperativoEventoHelper.resolverOperativoVisible(e, ahora);
            long inscritos = _inscripcionRepository.countCuposOcupadosPorEvento(e.getIdEvento());
            long asistencias = _inscripcionRepository.countAsistenciasConfirmadasPorEvento(e.getIdEvento());
            totalInscritos += inscritos;

            boolean activoCalendario = Boolean.TRUE.equals(e.getEstado())
                    && e.getFechaInicio() != null
                    && e.getFechaFin() != null
                    && !ahora.isBefore(e.getFechaInicio())
                    && !ahora.isAfter(e.getFechaFin());
            if (activoCalendario) {
                activos++;
            }

            if (op == EstadoOperativoEvento.EN_REVISION
                    || op == EstadoOperativoEvento.FINALIZADO_POR_TIEMPO) {
                eventosPorCertificar++;
            }

            ProfesorEventoTarjetaDTO tarjeta = construirTarjetaProfesor(cargarMonitoresSiFalta(e), op, inscritos, asistencias, ahora);
            clasificarTarjeta(tarjeta, op, enCurso, pendientesCierre, historial);

            if (op == EstadoOperativoEvento.EN_CURSO && esSesionHoy(e, hoy) && !banner.isSesionActivaHoy()) {
                Usuario monitor = primerMonitor(cargarMonitoresSiFalta(e));
                banner = ProfesorPanelBannerDTO.builder()
                        .sesionActivaHoy(true)
                        .idEvento(e.getIdEvento())
                        .nombreEvento(e.getNombreEvento())
                        .fechaInicio(e.getFechaInicio())
                        .fechaFin(e.getFechaFin())
                        .monitorNombre(monitor != null ? monitor.getNombres() : null)
                        .monitorApellidos(monitor != null ? monitor.getApellidos() : null)
                        .build();
            }

            resumenes.add(ProfesorEventoResumenDTO.builder()
                    .idEvento(e.getIdEvento())
                    .nombreEvento(e.getNombreEvento())
                    .tipoEvento(e.getTipoEvento() != null ? e.getTipoEvento().name() : null)
                    .activo(activoCalendario)
                    .estadoOperativo(op)
                    .inscritos(inscritos)
                    .fechaInicio(e.getFechaInicio())
                    .fechaFin(e.getFechaFin())
                    .build());
        }

        return ProfesorPanelDTO.builder()
                .totalEventos(eventos.size())
                .totalInscritos(totalInscritos)
                .eventosActivos(activos)
                .eventosPorCertificar(eventosPorCertificar)
                .accionesPendientes(eventosPorCertificar)
                .banner(banner)
                .enCurso(enCurso)
                .pendientesCierre(pendientesCierre)
                .historial(historial)
                .eventos(resumenes)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public EventoRevisionPanelDTO obtenerRevisionCierre(Long idEvento) {
        asegurarRolCrearEditar();
        Usuario profesor = _securityUsuarioHelper.usuarioAutenticado();
        Evento evento = _eventoRepository.findById(idEvento)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento no encontrado"));
        cargarStaffParaValidacion(evento);
        asegurarProfesorGestionaEvento(evento, profesor);

        LocalDateTime ahora = LocalDateTime.now();
        EstadoOperativoEvento op = EstadoOperativoEventoHelper.resolverOperativoVisible(evento, ahora);
        if (op != EstadoOperativoEvento.EN_REVISION
                && op != EstadoOperativoEvento.FINALIZADO_POR_TIEMPO) {
            throw new OperacionNoPermitidaException(
                    "La revisión de cierre solo está disponible para eventos en revisión o finalizados por tiempo.");
        }

        List<Inscripcion> inscripciones = _inscripcionRepository.findActivasPorEventoConUsuario(idEvento);
        List<EventoRevisionAlumnoDTO> alumnos = new ArrayList<>();

        for (Inscripcion ins : inscripciones) {
            Usuario u = ins.getUsuario();
            Double nota = _resultadoEvaluacionRepository
                    .findFirstByInscripcion_IdInscripcionOrderByIdDesc(ins.getIdInscripcion())
                    .map(r -> r.getNota())
                    .orElse(null);

            int pct = ProfesorAsistenciaHelper.porcentajeAsistenciaEstudiante(ins, evento, ahora);
            boolean asistio = InscripcionEstadoHelper.tieneAsistenciaConfirmada(ins.getEstado());
            boolean elegible = _elegibilidadService.puedeEmitirCertificado(ins);
            String motivo = elegible ? null : _elegibilidadService.motivoPendienteCertificado(ins);

            alumnos.add(EventoRevisionAlumnoDTO.builder()
                    .idInscripcion(ins.getIdInscripcion())
                    .nombres(u.getNombres())
                    .apellidos(u.getApellidos())
                    .correo(u.getCorreo())
                    .estadoInscripcion(ins.getEstado())
                    .nota(nota)
                    .porcentajeAsistencia(pct)
                    .asistenciaConfirmada(asistio)
                    .elegibleCertificado(elegible)
                    .motivoNoElegible(motivo)
                    .build());
        }

        long asistencias = _inscripcionRepository.countAsistenciasConfirmadasPorEvento(idEvento);

        return EventoRevisionPanelDTO.builder()
                .idEvento(idEvento)
                .nombreEvento(evento.getNombreEvento())
                .estadoOperativo(op)
                .totalInscritos(inscripciones.size())
                .asistenciasRegistradas(asistencias)
                .alumnos(alumnos)
                .build();
    }

    /**
     * Evita MultipleBagFetchException: no hace JOIN FETCH de dos colecciones ManyToMany a la vez.
     */
    private List<Evento> listarEventosGestionadosPorProfesor(Long idProfesor) {
        Map<Long, Evento> porId = new LinkedHashMap<>();
        for (Evento e : _eventoRepository.findByUsuarioCreador_IdUsuarioOrderByFechaInicioDesc(idProfesor)) {
            porId.put(e.getIdEvento(), e);
        }
        for (Long idEvento : _eventoRepository.findIdsEventoDondeEsColaborador(idProfesor)) {
            if (!porId.containsKey(idEvento)) {
                _eventoRepository.findById(idEvento).ifPresent(ev -> porId.put(idEvento, ev));
            }
        }
        return porId.values().stream()
                .sorted(Comparator.comparing(Evento::getFechaInicio, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private Evento cargarMonitoresSiFalta(Evento evento) {
        if (evento.getMonitoresAsignados() != null && !evento.getMonitoresAsignados().isEmpty()) {
            return evento;
        }
        return _eventoRepository.findByIdConMonitores(evento.getIdEvento()).orElse(evento);
    }

    private void cargarStaffParaValidacion(Evento evento) {
        _eventoRepository.findByIdConProfesores(evento.getIdEvento())
                .ifPresent(e -> evento.setProfesoresColaboradores(e.getProfesoresColaboradores()));
    }

    private static ProfesorEventoTarjetaDTO construirTarjetaProfesor(
            Evento e,
            EstadoOperativoEvento op,
            long inscritos,
            long asistencias,
            LocalDateTime ahora) {
        Usuario monitor = primerMonitor(e);
        return ProfesorEventoTarjetaDTO.builder()
                .idEvento(e.getIdEvento())
                .nombreEvento(e.getNombreEvento())
                .tipoEvento(e.getTipoEvento() != null ? e.getTipoEvento().name() : null)
                .estadoOperativo(op)
                .inscritosActivos(inscritos)
                .asistenciasConfirmadas(asistencias)
                .porcentajeAsistenciaGlobal(ProfesorAsistenciaHelper.porcentajeAsistenciaGlobal(asistencias, inscritos))
                .fechaInicio(e.getFechaInicio())
                .fechaFin(e.getFechaFin())
                .monitorNombre(monitor != null ? monitor.getNombres() : null)
                .monitorApellidos(monitor != null ? monitor.getApellidos() : null)
                .requiereIniciarRevision(op == EstadoOperativoEvento.FINALIZADO_POR_TIEMPO)
                .build();
    }

    private static void clasificarTarjeta(
            ProfesorEventoTarjetaDTO tarjeta,
            EstadoOperativoEvento op,
            List<ProfesorEventoTarjetaDTO> enCurso,
            List<ProfesorEventoTarjetaDTO> pendientesCierre,
            List<ProfesorEventoTarjetaDTO> historial) {
        switch (op) {
            case EN_CURSO -> enCurso.add(tarjeta);
            case EN_REVISION, FINALIZADO_POR_TIEMPO -> pendientesCierre.add(tarjeta);
            case CERRADO_Y_CERTIFICADO -> historial.add(tarjeta);
            default -> {
                if (op != EstadoOperativoEvento.EVENT_CANCELLED && op != EstadoOperativoEvento.PROXIMO) {
                    pendientesCierre.add(tarjeta);
                }
            }
        }
    }

    private static boolean esSesionHoy(Evento e, LocalDate hoy) {
        if (e.getFechaInicio() == null || e.getFechaFin() == null) {
            return false;
        }
        LocalDate inicio = e.getFechaInicio().toLocalDate();
        LocalDate fin = e.getFechaFin().toLocalDate();
        return !hoy.isBefore(inicio) && !hoy.isAfter(fin);
    }

    private static Usuario primerMonitor(Evento e) {
        if (e.getMonitoresAsignados() == null || e.getMonitoresAsignados().isEmpty()) {
            return null;
        }
        return e.getMonitoresAsignados().iterator().next();
    }

    private void asegurarProfesorGestionaEvento(Evento evento, Usuario profesor) {
        if (EventoEdicionPolicy.esAdminAutenticado()) {
            return;
        }
        Long id = profesor.getIdUsuario();
        boolean creador = evento.getUsuarioCreador() != null
                && id.equals(evento.getUsuarioCreador().getIdUsuario());
        boolean colaborador = evento.getProfesoresColaboradores() != null
                && evento.getProfesoresColaboradores().stream()
                        .anyMatch(p -> id.equals(p.getIdUsuario()));
        if (!creador && !colaborador) {
            throw new OperacionNoPermitidaException("No tiene permiso para gestionar este evento.");
        }
    }

    private void asegurarCodigoDifusion(Evento evento) {
        if (evento.getCodigoDifusion() == null || evento.getCodigoDifusion().isBlank()) {
            evento.setCodigoDifusion(UUID.randomUUID().toString().replace("-", ""));
            _eventoRepository.save(evento);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EventoCupoVerificacionDTO verificarCupo(Long idEvento) {
        Evento evento = _eventoRepository.findById(idEvento)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento no encontrado"));
        long ocupados = _inscripcionRepository.countCuposOcupadosPorEvento(idEvento);
        Integer cap = evento.getAforoMaximo();
        boolean hay = cap == null || ocupados < cap;
        return EventoCupoVerificacionDTO.builder()
                .inscritosActivos(ocupados)
                .aforoMaximo(cap)
                .hayCupoDisponible(hay)
                .build();
    }

    private Evento nuevaInstancia(TipoEventoEnum tipo) {
        return switch (tipo) {
            case CURSO -> new CursoEvento();
            case HACKATHON -> new HackathonEvento();
            case FERIA -> new FeriaProyectoEvento();
            case TALLER -> new TallerEvento();
        };
    }

    private void aplicarCamposComunes(Evento evento, CrearEventoDTO dto) {
        evento.setNombreEvento(dto.getNombreEvento());
        evento.setDescripcion(dto.getDescripcion());
        evento.setTipoEvento(dto.getTipoEvento());
        _catalogoHelper.asignarCatalogoSiFalta(evento);
        evento.setModalidad(dto.getModalidad());
        evento.setFechaInicio(dto.getFechaInicio());
        evento.setFechaFin(dto.getFechaFin());
        evento.setUbicacion(dto.getUbicacion());
        evento.setEnlaceVirtual(dto.getEnlaceVirtual());
        evento.setAforoMaximo(dto.getAforoMaximo());
        evento.setIntensidadHoraria(dto.getIntensidadHoraria());
        evento.setCosto(dto.getPrecio() != null ? dto.getPrecio() : 0.0);
        evento.setTextoDiploma(dto.getTextoDiploma());
        evento.setFirmaDigitalProfesor(dto.getFirmaDigitalProfesor());
    }

    private void aplicarDetalleEnEntidad(Evento evento, CrearEventoDTO dto) {
        switch (dto.getTipoEvento()) {
            case CURSO -> aplicarCurso((CursoEvento) evento, dto.getDetalleCurso());
            case HACKATHON -> aplicarHackathon((HackathonEvento) evento, dto.getDetalleHackathon());
            case FERIA -> aplicarFeria((FeriaProyectoEvento) evento, dto.getDetalleFeria());
            case TALLER -> aplicarTaller((TallerEvento) evento, dto.getDetalleTaller());
        }
    }

    private void aplicarDetalleEnEntidadDesdeDto(Evento evento, EventoDTO dto) {
        switch (evento.getTipoEvento()) {
            case CURSO -> aplicarCurso((CursoEvento) evento, dto.getDetalleCurso());
            case HACKATHON -> aplicarHackathon((HackathonEvento) evento, dto.getDetalleHackathon());
            case FERIA -> aplicarFeria((FeriaProyectoEvento) evento, dto.getDetalleFeria());
            case TALLER -> aplicarTaller((TallerEvento) evento, dto.getDetalleTaller());
        }
    }

    private void aplicarCurso(CursoEvento c, DetalleCursoDTO d) {
        if (d == null) {
            throw new OperacionNoPermitidaException("Los datos específicos del curso son obligatorios.");
        }
        if (d.getNivelAcademico() == null) {
            throw new OperacionNoPermitidaException("El nivel académico del curso es obligatorio.");
        }
        if (d.getNotaMinimaAprobacion() == null) {
            throw new OperacionNoPermitidaException("La nota mínima de aprobación del curso es obligatoria.");
        }
        if (d.getPorcentajeAsistenciaMinimo() == null) {
            throw new OperacionNoPermitidaException("El porcentaje de asistencia mínima del curso es obligatorio.");
        }
        c.setNivelAcademico(d.getNivelAcademico());
        c.setNotaMinimaAprobacion(d.getNotaMinimaAprobacion());
        c.setPorcentajeAsistenciaMinimo(d.getPorcentajeAsistenciaMinimo());
    }

    private void validarEntidadAntesDeGuardar(Evento evento) {
        if (evento.getNombreEvento() == null || evento.getNombreEvento().isBlank()) {
            throw new OperacionNoPermitidaException("El nombre del evento es obligatorio.");
        }
        if (evento.getFechaInicio() == null || evento.getFechaFin() == null) {
            throw new OperacionNoPermitidaException("Las fechas de inicio y fin son obligatorias.");
        }
        if (evento.getAforoMaximo() == null || evento.getAforoMaximo() < 1) {
            throw new OperacionNoPermitidaException("El aforo máximo debe ser al menos 1.");
        }
        if (evento.getIntensidadHoraria() == null || evento.getIntensidadHoraria() < 1) {
            throw new OperacionNoPermitidaException("La intensidad horaria debe ser al menos 1.");
        }
        if (evento.getUsuarioCreador() == null) {
            throw new OperacionNoPermitidaException("No se pudo asignar el usuario creador del evento.");
        }
        if (evento.getCatalogoTipoEvento() == null) {
            throw new OperacionNoPermitidaException("No se pudo asociar el tipo de evento al catálogo (id_tipo_evento).");
        }
        if (evento instanceof CursoEvento curso) {
            if (curso.getNivelAcademico() == null) {
                throw new OperacionNoPermitidaException("El nivel académico del curso es obligatorio.");
            }
            if (curso.getNotaMinimaAprobacion() == null) {
                throw new OperacionNoPermitidaException("La nota mínima del curso es obligatoria.");
            }
            if (curso.getPorcentajeAsistenciaMinimo() == null) {
                throw new OperacionNoPermitidaException("La asistencia mínima del curso es obligatoria.");
            }
        }
    }

    private void aplicarHackathon(HackathonEvento h, DetalleHackathonDTO d) {
        if (d == null) {
            return;
        }
        h.setRetoTecnicoCentral(d.getRetoTecnicoCentral());
        h.setMinIntegrantes(d.getMinIntegrantes());
        h.setMaxIntegrantes(d.getMaxIntegrantes());
        h.setPremiosIncentivos(d.getPremiosIncentivos());
    }

    private void aplicarFeria(FeriaProyectoEvento f, DetalleFeriaDTO d) {
        if (d == null) {
            return;
        }
        f.setCategoriaExhibicion(d.getCategoriaExhibicion());
        f.setStackTecnologico(d.getStackTecnologico());
        f.setCriteriosEvaluacion(d.getCriteriosEvaluacion());
    }

    private void aplicarTaller(TallerEvento t, DetalleTallerDTO d) {
        if (d == null) {
            return;
        }
        t.setMaterialGuia(d.getMaterialGuia());
    }

    private void validarDetallePorTipo(CrearEventoDTO dto) {
        switch (dto.getTipoEvento()) {
            case CURSO -> {
                if (dto.getDetalleCurso() == null) {
                    throw new OperacionNoPermitidaException("Los datos específicos del curso son obligatorios.");
                }
            }
            case HACKATHON -> {
                if (dto.getDetalleHackathon() == null) {
                    throw new OperacionNoPermitidaException("Los datos específicos del hackathon son obligatorios.");
                }
                var h = dto.getDetalleHackathon();
                if (h.getMaxIntegrantes() < h.getMinIntegrantes()) {
                    throw new OperacionNoPermitidaException("El máximo de integrantes debe ser >= al mínimo.");
                }
            }
            case FERIA -> {
                if (dto.getDetalleFeria() == null) {
                    throw new OperacionNoPermitidaException("Los datos específicos de la feria son obligatorios.");
                }
                validarJsonArray(dto.getDetalleFeria().getStackTecnologico(), "stack tecnológico");
            }
            case TALLER -> { }
        }
    }

    private void validarJsonArray(String json, String campo) {
        try {
            List<String> list = _objectMapper.readValue(json.trim(), new TypeReference<List<String>>() {});
            if (list == null || list.isEmpty()) {
                throw new OperacionNoPermitidaException("El " + campo + " debe ser un JSON array no vacío.");
            }
        } catch (OperacionNoPermitidaException e) {
            throw e;
        } catch (Exception e) {
            throw new OperacionNoPermitidaException("Formato inválido en " + campo + " (se espera JSON array).");
        }
    }

    /**
     * Carga usuarios que poseen exclusivamente el rol solicitado (staff académico o monitor).
     * Evita asignar estudiantes como monitores.
     */
    private void normalizarStaffEnDto(CrearEventoDTO dto) {
        List<Long> profesores = normalizarIdsUsuarios(dto.getIdsProfesoresColaboradores());
        List<Long> monitores = normalizarIdsUsuarios(dto.getIdsMonitoresAsignados());
        validarCruceStaff(profesores, monitores);
        dto.setIdsProfesoresColaboradores(profesores);
        dto.setIdsMonitoresAsignados(monitores);
    }

    private void normalizarStaffEnEventoDto(EventoDTO dto) {
        if (dto.getIdsProfesoresColaboradores() != null) {
            dto.setIdsProfesoresColaboradores(normalizarIdsUsuarios(dto.getIdsProfesoresColaboradores()));
        }
        if (dto.getIdsMonitoresAsignados() != null) {
            dto.setIdsMonitoresAsignados(normalizarIdsUsuarios(dto.getIdsMonitoresAsignados()));
        }
        List<Long> profesores = dto.getIdsProfesoresColaboradores() != null
                ? dto.getIdsProfesoresColaboradores()
                : List.of();
        List<Long> monitores = dto.getIdsMonitoresAsignados() != null
                ? dto.getIdsMonitoresAsignados()
                : List.of();
        if (!profesores.isEmpty() && !monitores.isEmpty()) {
            validarCruceStaff(profesores, monitores);
        }
    }

    private void validarCruceStaff(List<Long> profesores, List<Long> monitores) {
        Set<Long> cruce = new HashSet<>(profesores);
        cruce.retainAll(monitores);
        if (!cruce.isEmpty()) {
            throw new OperacionNoPermitidaException(
                    "Un mismo usuario no puede ser profesor colaborador y monitor en el mismo evento.");
        }
    }

    private List<Long> normalizarIdsUsuarios(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ids.stream()
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .toList();
    }

    private void asegurarCodigoDifusionUnico(Evento evento) {
        if (evento.getCodigoDifusion() == null || evento.getCodigoDifusion().isBlank()) {
            evento.setCodigoDifusion(generarCodigoDifusion());
            return;
        }
        String codigo = evento.getCodigoDifusion().trim();
        if (_eventoRepository.existsByCodigoDifusion(codigo)) {
            evento.setCodigoDifusion(generarCodigoDifusion());
        } else {
            evento.setCodigoDifusion(codigo);
        }
    }

    private String generarCodigoDifusion() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private Set<Usuario> cargarUsuariosPorRolExclusivo(List<Long> ids, String rolRequerido) {
        List<Long> idsUnicos = normalizarIdsUsuarios(ids);
        if (idsUnicos.isEmpty()) {
            return new HashSet<>();
        }
        List<Usuario> usuarios = _usuarioRepository.findAllById(idsUnicos);
        if (usuarios.size() != idsUnicos.size()) {
            Set<Long> encontrados = usuarios.stream().map(Usuario::getIdUsuario).collect(Collectors.toSet());
            List<Long> faltantes = idsUnicos.stream().filter(id -> !encontrados.contains(id)).toList();
            throw new RecursoNoEncontradoException(
                    "Usuarios del staff no encontrados (IDs: " + faltantes + ").");
        }
        Set<Usuario> resultado = new HashSet<>();
        for (Usuario u : usuarios) {
            if (!Boolean.TRUE.equals(u.getEstado())) {
                throw new OperacionNoPermitidaException("El usuario " + u.getCorreo() + " está inactivo.");
            }
            boolean tieneRol = u.getRoles().stream().anyMatch(r -> rolRequerido.equals(r.getNombre()));
            if (!tieneRol) {
                throw new OperacionNoPermitidaException(
                        "El usuario " + u.getCorreo() + " no tiene el rol " + rolRequerido.replace("ROLE_", "") + ".");
            }
            if (ROL_MONITOR.equals(rolRequerido) && u.getRoles().stream().anyMatch(r -> "ROLE_ESTUDIANTE".equals(r.getNombre()))) {
                throw new OperacionNoPermitidaException(
                        "El usuario " + u.getCorreo() + " es estudiante y no puede asignarse como monitor.");
            }
            resultado.add(u);
        }
        return resultado;
    }

    private static Boolean resolverFiltroActivo(Boolean soloActivos) {
        if (soloActivos == null) {
            return Boolean.TRUE;
        }
        return soloActivos ? Boolean.TRUE : null;
    }

    private void validarFechas(LocalDateTime inicio, LocalDateTime fin, boolean validarInicioNoPasado) {
        if (fin.isBefore(inicio)) {
            throw new OperacionNoPermitidaException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }
        if (validarInicioNoPasado && inicio.isBefore(LocalDateTime.now())) {
            throw new OperacionNoPermitidaException("La fecha de inicio no puede ser en el pasado.");
        }
    }

    private void asegurarRolCrearEditar() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new OperacionNoPermitidaException("Debe iniciar sesión para esta operación.");
        }
        boolean permitido = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(r -> "ROLE_ADMIN".equals(r) || "ROLE_PROFESOR".equals(r));
        if (!permitido) {
            throw new OperacionNoPermitidaException("Solo ADMIN o PROFESOR pueden gestionar eventos.");
        }
    }

    private Usuario usuarioActualDesdeSeguridad() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            throw new OperacionNoPermitidaException("No se pudo determinar el usuario autenticado.");
        }
        return _usuarioRepository.findByCorreo(auth.getName())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    private List<CampoFormularioDTO> camposADto(Long idEvento) {
        return _campoFormularioRepository.findByEvento_IdEventoOrderByIdCampoAsc(idEvento).stream()
                .map(this::campoToDto)
                .toList();
    }

    private CampoFormularioDTO campoToDto(CampoFormulario c) {
        Long idEv = c.getEvento() != null ? c.getEvento().getIdEvento() : null;
        return new CampoFormularioDTO(
                c.getIdCampo(),
                idEv,
                c.getEtiqueta(),
                c.getTipoDato(),
                c.isEsObligatorio(),
                c.getOpciones());
    }

    private List<CrearCampoFormularioDTO> mapDtoACrear(List<CampoFormularioDTO> dtos) {
        List<CrearCampoFormularioDTO> out = new ArrayList<>();
        for (CampoFormularioDTO d : dtos) {
            CrearCampoFormularioDTO x = new CrearCampoFormularioDTO();
            x.setEtiqueta(d.getEtiqueta());
            x.setTipoDato(d.getTipoDato());
            x.setEsObligatorio(d.isEsObligatorio());
            x.setOpciones(d.getOpciones());
            out.add(x);
        }
        return out;
    }

    private void persistirCamposDesdeCrear(Evento evento, List<CrearCampoFormularioDTO> defs) {
        if (defs == null || defs.isEmpty()) {
            return;
        }
        for (CrearCampoFormularioDTO d : defs) {
            validarDefCampo(d);
            CampoFormulario c = new CampoFormulario();
            c.setEvento(evento);
            c.setEtiqueta(d.getEtiqueta().trim());
            c.setTipoDato(d.getTipoDato());
            c.setEsObligatorio(d.isEsObligatorio());
            c.setOpciones(normalizarOpcionesSiSelect(d));
            _campoFormularioRepository.save(c);
        }
    }

    private void validarDefCampo(CrearCampoFormularioDTO d) {
        if (d.getTipoDato() == TipoDatoCampo.SELECT) {
            if (d.getOpciones() == null || d.getOpciones().isBlank()) {
                throw new OperacionNoPermitidaException("Los campos tipo SELECT requieren opciones (JSON array de strings).");
            }
            parseOpcionesJson(d.getOpciones());
        }
    }

    private String normalizarOpcionesSiSelect(CrearCampoFormularioDTO d) {
        if (d.getTipoDato() != TipoDatoCampo.SELECT) {
            return null;
        }
        try {
            List<String> raw = _objectMapper.readValue(d.getOpciones().trim(), new TypeReference<List<String>>() {});
            List<String> trimmed = raw.stream().map(String::trim).filter(s -> !s.isEmpty()).toList();
            if (trimmed.isEmpty()) {
                throw new OperacionNoPermitidaException("Las opciones SELECT no pueden estar vacías.");
            }
            return _objectMapper.writeValueAsString(trimmed);
        } catch (OperacionNoPermitidaException e) {
            throw e;
        } catch (Exception e) {
            throw new OperacionNoPermitidaException("Opciones inválidas (se espera JSON array de strings).");
        }
    }

    private Evento cargarEventoConStaff(Long idEvento) {
        Evento evento = _eventoRepository.findByIdConProfesores(idEvento)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento no encontrado"));
        _eventoRepository.findByIdConMonitores(idEvento)
                .ifPresent(otro -> evento.setMonitoresAsignados(otro.getMonitoresAsignados()));
        return evento;
    }

    /** Evita fallos al mapear colecciones LAZY tras el guardado (open-in-view=false). */
    private EventoDTO construirDtoPostGuardado(
            Evento evento,
            Set<Usuario> profesores,
            Set<Usuario> monitores,
            Usuario creador) {
        EventoDTO salida = _eventoMapper.toDto(evento, false);
        salida.setIdUsuarioCreador(creador.getIdUsuario());
        salida.setProfesoresColaboradores(
                profesores.stream().map(_eventoMapper::toStaff).toList());
        salida.setMonitoresAsignados(
                monitores.stream().map(_eventoMapper::toStaff).toList());
        salida.setCamposPersonalizados(camposADto(evento.getIdEvento()));
        return salida;
    }

    private List<String> parseOpcionesJson(String json) {
        try {
            List<String> list = _objectMapper.readValue(json.trim(), new TypeReference<List<String>>() {});
            if (list == null || list.isEmpty()) {
                throw new OperacionNoPermitidaException("Las opciones SELECT deben ser un JSON array no vacío.");
            }
            return list;
        } catch (OperacionNoPermitidaException e) {
            throw e;
        } catch (Exception e) {
            throw new OperacionNoPermitidaException("Opciones inválidas (se espera JSON array de strings).");
        }
    }
}
