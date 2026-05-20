package com.certiva.api.Impl;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.certiva.api.Config.JwtUtil;
import com.certiva.api.DTO.CrearUsuarioDTO;
import com.certiva.api.DTO.ImportacionCsvResultadoDTO;
import com.certiva.api.DTO.LoginDTO;
import com.certiva.api.DTO.LoginRespuestaDTO;
import com.certiva.api.DTO.RolResumenDTO;
import com.certiva.api.DTO.TipoDocumentoResumenDTO;
import com.certiva.api.DTO.UsuarioDTO;
import com.certiva.api.DTO.UsuarioStaffDTO;
import com.certiva.api.Util.EventoMapper;
import com.certiva.api.Entity.Rol;
import com.certiva.api.Entity.TipoDocumento;
import com.certiva.api.Entity.Usuario;
import com.certiva.api.Exception.CredencialesInvalidasException;
import com.certiva.api.Exception.OperacionNoPermitidaException;
import com.certiva.api.Exception.RecursoDuplicadoException;
import com.certiva.api.Exception.RecursoNoEncontradoException;
import com.certiva.api.Exception.UsuarioInactivoException;
import com.certiva.api.Repository.RolRepository;
import com.certiva.api.Repository.TipoDocumentoRepository;
import com.certiva.api.Repository.UsuarioRepository;
import com.certiva.api.Service.AuditoriaService;
import com.certiva.api.Service.UsuarioService;
import com.certiva.api.Util.SecurityUsuarioHelper;
import com.opencsv.CSVReader;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository _usuarioRepository;
    private final TipoDocumentoRepository _tipoDocumentoRepository;
    private final RolRepository _rolRepository;
    private final AuditoriaService _auditoriaService;
    private final ModelMapper _modelMapper;
    private final BCryptPasswordEncoder _passwordEncoder;
    private final JwtUtil _jwtUtil;
    private final EventoMapper _eventoMapper;
    private final SecurityUsuarioHelper _securityUsuarioHelper;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
                              TipoDocumentoRepository tipoDocumentoRepository,
                              RolRepository rolRepository,
                              AuditoriaService auditoriaService,
                              ModelMapper modelMapper,
                              BCryptPasswordEncoder passwordEncoder,
                              JwtUtil jwtUtil,
                              EventoMapper eventoMapper,
                              SecurityUsuarioHelper securityUsuarioHelper) {
        this._usuarioRepository = usuarioRepository;
        this._tipoDocumentoRepository = tipoDocumentoRepository;
        this._rolRepository = rolRepository;
        this._auditoriaService = auditoriaService;
        this._modelMapper = modelMapper;
        this._passwordEncoder = passwordEncoder;
        this._jwtUtil = jwtUtil;
        this._eventoMapper = eventoMapper;
        this._securityUsuarioHelper = securityUsuarioHelper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioStaffDTO> listarStaffPorRol(String codigoRol, String busqueda) {
        String nombreRol = normalizarNombreRol(codigoRol);
        if (!"ROLE_PROFESOR".equals(nombreRol) && !"ROLE_MONITOR".equals(nombreRol)) {
            throw new OperacionNoPermitidaException("Solo se permite buscar staff con rol PROFESOR o MONITOR.");
        }
        String q = busqueda != null ? busqueda.trim() : "";
        return _usuarioRepository.buscarActivosPorRol(nombreRol, q.isEmpty() ? null : q).stream()
                .map(_eventoMapper::toStaff)
                .toList();
    }

    private static String normalizarNombreRol(String codigo) {
        String upper = codigo.trim().toUpperCase();
        if (!upper.startsWith("ROLE_")) {
            upper = "ROLE_" + upper;
        }
        return upper;
    }

    @Override
    public CrearUsuarioDTO registrarUsuario(CrearUsuarioDTO usuarioDTO) {
        if (_usuarioRepository.existsByCorreo(usuarioDTO.getCorreo())) {
            throw new RecursoDuplicadoException("Correo ya registrado");
        }

        if (_usuarioRepository.existsByNumeroDocumento(usuarioDTO.getNumeroDocumento())) {
            throw new RecursoDuplicadoException("Número de documento ya registrado");
        }

        TipoDocumento tipoDocumento = _tipoDocumentoRepository.findById(usuarioDTO.getIdTipoDocumento())
                .orElseThrow(() -> new RecursoNoEncontradoException("Tipo de documento no encontrado"));

        Rol rol = _rolRepository.findById(usuarioDTO.getIdRol())
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado"));

        if (!"ROLE_ESTUDIANTE".equals(rol.getNombre())) {
            throw new OperacionNoPermitidaException(
                    "El registro público solo permite el rol de estudiante");
        }

        Usuario usuario = new Usuario();
        usuario.setNombres(usuarioDTO.getNombres());
        usuario.setApellidos(usuarioDTO.getApellidos());
        usuario.setNumeroDocumento(usuarioDTO.getNumeroDocumento());
        usuario.setCorreo(usuarioDTO.getCorreo());
        usuario.setContraseña(_passwordEncoder.encode(usuarioDTO.getContraseña()));
        usuario.setEstado(true);
        usuario.setFechaRegistro(LocalDateTime.now());
        usuario.setTipoDocumento(tipoDocumento);
        usuario.setRoles(Set.of(rol));

        usuario = _usuarioRepository.save(usuario);
        return _modelMapper.map(usuario, CrearUsuarioDTO.class);
    }

    @Override
    public CrearUsuarioDTO crearUsuarioAdministracion(CrearUsuarioDTO usuarioDTO) {
        if (_usuarioRepository.existsByCorreo(usuarioDTO.getCorreo())) {
            throw new RecursoDuplicadoException("Correo ya registrado");
        }

        if (_usuarioRepository.existsByNumeroDocumento(usuarioDTO.getNumeroDocumento())) {
            throw new RecursoDuplicadoException("Número de documento ya registrado");
        }

        TipoDocumento tipoDocumento = _tipoDocumentoRepository.findById(usuarioDTO.getIdTipoDocumento())
                .orElseThrow(() -> new RecursoNoEncontradoException("Tipo de documento no encontrado"));

        Rol rol = _rolRepository.findById(usuarioDTO.getIdRol())
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado"));

        validarPoliticaAdministradoresAlCrear(rol);

        Usuario usuario = new Usuario();
        usuario.setNombres(usuarioDTO.getNombres());
        usuario.setApellidos(usuarioDTO.getApellidos());
        usuario.setNumeroDocumento(usuarioDTO.getNumeroDocumento());
        usuario.setCorreo(usuarioDTO.getCorreo());
        usuario.setContraseña(_passwordEncoder.encode(usuarioDTO.getContraseña()));
        usuario.setEstado(true);
        usuario.setFechaRegistro(LocalDateTime.now());
        usuario.setTipoDocumento(tipoDocumento);
        usuario.setRoles(Set.of(rol));

        usuario = _usuarioRepository.save(usuario);
        return _modelMapper.map(usuario, CrearUsuarioDTO.class);
    }

    @Override
    public LoginRespuestaDTO login(LoginDTO loginDTO, String ip) {
        Usuario usuario = _usuarioRepository.findByCorreo(loginDTO.getCorreo())
                .orElseThrow(() -> {
                    _auditoriaService.registrarAuditoria("LOGIN_FALLIDO",
                            "Intento de login con correo no registrado: " + loginDTO.getCorreo(), ip, null);
                    return new RecursoNoEncontradoException("Usuario no encontrado");
                });

        if (!_passwordEncoder.matches(loginDTO.getPassword(), usuario.getContraseña())) {
            _auditoriaService.registrarAuditoria("LOGIN_FALLIDO",
                    "Credenciales inválidas para: " + loginDTO.getCorreo(), ip, usuario);
            throw new CredencialesInvalidasException("Credenciales inválidas");
        }

        if (!usuario.getEstado()) {
            _auditoriaService.registrarAuditoria("LOGIN_FALLIDO",
                    "Intento de login de usuario inactivo: " + loginDTO.getCorreo(), ip, usuario);
            throw new UsuarioInactivoException("Usuario inactivo");
        }

        _auditoriaService.registrarAuditoria("LOGIN_EXITOSO",
                "Login exitoso para: " + loginDTO.getCorreo(), ip, usuario);

        String rolPrincipal = obtenerRolPrincipal(usuario);
        String token = _jwtUtil.generateToken(usuario.getCorreo(), rolPrincipal);

        UsuarioDTO usuarioDTO = mapearUsuarioDTO(usuario);

        return new LoginRespuestaDTO("Login exitoso", token, usuarioDTO);
    }

    @Override
    public UsuarioDTO obtenerUsuarioPorId(Long idUsuario) {
        Usuario usuario = _usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        return mapearUsuarioDTO(usuario);
    }

    @Override
    public List<UsuarioDTO> listarUsuarios() {
        return _usuarioRepository.findAll().stream()
                .map(this::mapearUsuarioDTO)
                .toList();
    }

    @Override
    public UsuarioDTO actualizarUsuario(UsuarioDTO usuarioDTO) {
        Usuario usuario = _usuarioRepository.findById(usuarioDTO.getIdUsuario())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        if (_usuarioRepository.existsByCorreoAndIdUsuarioNot(usuarioDTO.getCorreo(), usuarioDTO.getIdUsuario())) {
            throw new RecursoDuplicadoException("Correo ya registrado");
        }

        if (_usuarioRepository.existsByNumeroDocumentoAndIdUsuarioNot(usuarioDTO.getNumeroDocumento(),
                usuarioDTO.getIdUsuario())) {
            throw new RecursoDuplicadoException("Número de documento ya registrado");
        }

        usuario.setNombres(usuarioDTO.getNombres());
        usuario.setApellidos(usuarioDTO.getApellidos());
        usuario.setNumeroDocumento(usuarioDTO.getNumeroDocumento());
        usuario.setCorreo(usuarioDTO.getCorreo());
        if (usuarioDTO.getTelefono() != null) {
            usuario.setTelefono(usuarioDTO.getTelefono().trim().isEmpty() ? null : usuarioDTO.getTelefono().trim());
        }
        if (usuarioDTO.getEstado() != null) {
            usuario.setEstado(usuarioDTO.getEstado());
        }

        if (usuarioDTO.getIdTipoDocumento() != null) {
            TipoDocumento tipoDocumento = _tipoDocumentoRepository.findById(usuarioDTO.getIdTipoDocumento())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Tipo de documento no encontrado"));
            usuario.setTipoDocumento(tipoDocumento);
        }

        if (usuarioDTO.getIdRol() != null) {
            Rol rol = _rolRepository.findById(usuarioDTO.getIdRol())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado"));
            validarPoliticaAdministradoresTrasCambioRol(usuario.getIdUsuario(), rol);
            usuario.setRoles(Set.of(rol));
        }

        usuario = _usuarioRepository.save(usuario);
        return mapearUsuarioDTO(usuario);
    }

    @Override
    public UsuarioDTO cambiarRolUsuario(Long idUsuario, Long idRol) {
        Usuario usuario = _usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        Rol rol = _rolRepository.findById(idRol)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado"));

        validarPoliticaAdministradoresTrasCambioRol(usuario.getIdUsuario(), rol);
        usuario.setRoles(Set.of(rol));

        usuario = _usuarioRepository.save(usuario);
        return mapearUsuarioDTO(usuario);
    }

    @Override
    public String inactivarUsuario(Long idUsuario) {
        Usuario usuario = _usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        boolean esAdmin = usuario.getRoles().stream()
                .anyMatch(r -> "ROLE_ADMIN".equals(r.getNombre()));
        if (esAdmin) {
            long otrosAdmins = _usuarioRepository.countUsuariosConRolExceptoUsuario("ROLE_ADMIN", idUsuario);
            if (otrosAdmins < 1) {
                throw new OperacionNoPermitidaException("No se puede inactivar al único administrador del sistema.");
            }
        }

        usuario.setEstado(false);
        _usuarioRepository.save(usuario);
        return "Usuario inactivado correctamente";
    }

    @Override
    @Transactional
    public ImportacionCsvResultadoDTO importarUsuariosDesdeCsv(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("Archivo CSV vacío");
        }

        List<String> errores = new ArrayList<>();
        int exitosos = 0;

        try (CSVReader reader = new CSVReader(new InputStreamReader(archivo.getInputStream(), StandardCharsets.UTF_8))) {
            String[] cabecera = reader.readNext();
            if (cabecera == null) {
                throw new IllegalArgumentException("CSV sin contenido");
            }

            List<String[]> filas = new ArrayList<>();
            String[] fila;
            while ((fila = reader.readNext()) != null) {
                filas.add(fila);
            }

            List<String> correosCsv = new ArrayList<>();
            List<String> docsCsv = new ArrayList<>();
            for (String[] r : filas) {
                if (r.length >= 4) {
                    correosCsv.add(r[3].trim().toLowerCase());
                }
                if (r.length >= 3) {
                    docsCsv.add(r[2].trim());
                }
            }

            Set<String> correosEnBd = correosCsv.isEmpty()
                    ? new HashSet<>()
                    : new HashSet<>(_usuarioRepository.findCorreosExistentes(correosCsv));
            Set<String> docsEnBd = docsCsv.isEmpty()
                    ? new HashSet<>()
                    : new HashSet<>(_usuarioRepository.findDocumentosExistentes(docsCsv));

            Set<String> vistosCorreoArchivo = new HashSet<>();
            Set<String> vistosDocArchivo = new HashSet<>();

            int numeroFila = 1;
            for (String[] r : filas) {
                numeroFila++;
                try {
                    if (r.length < 6) {
                        errores.add("Fila " + numeroFila + ": se requieren columnas nombres,apellidos,numeroDocumento,correo,idTipoDocumento,idRol");
                        continue;
                    }

                    String nombres = r[0].trim();
                    String apellidos = r[1].trim();
                    String numeroDocumento = r[2].trim();
                    String correo = r[3].trim().toLowerCase();
                    long idTipoDocumento = Long.parseLong(r[4].trim());
                    long idRol = Long.parseLong(r[5].trim());

                    if (nombres.isEmpty() || apellidos.isEmpty() || numeroDocumento.isEmpty() || correo.isEmpty()) {
                        errores.add("Fila " + numeroFila + ": campos obligatorios vacíos");
                        continue;
                    }

                    if (vistosCorreoArchivo.contains(correo)) {
                        errores.add("Fila " + numeroFila + ": correo duplicado en el archivo");
                        continue;
                    }
                    if (vistosDocArchivo.contains(numeroDocumento)) {
                        errores.add("Fila " + numeroFila + ": documento duplicado en el archivo");
                        continue;
                    }

                    if (correosEnBd.contains(correo)) {
                        errores.add("Fila " + numeroFila + ": el correo ya existe en la base de datos");
                        continue;
                    }
                    if (docsEnBd.contains(numeroDocumento)) {
                        errores.add("Fila " + numeroFila + ": el documento ya existe en la base de datos");
                        continue;
                    }

                    TipoDocumento tipoDocumento = _tipoDocumentoRepository.findById(idTipoDocumento)
                            .orElseThrow(() -> new RecursoNoEncontradoException("Tipo de documento no encontrado"));

                    Rol rol = _rolRepository.findById(idRol)
                            .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado"));

                    validarPoliticaAdministradoresAlCrear(rol);

                    Usuario usuario = new Usuario();
                    usuario.setNombres(nombres);
                    usuario.setApellidos(apellidos);
                    usuario.setNumeroDocumento(numeroDocumento);
                    usuario.setCorreo(correo);
                    usuario.setContraseña(_passwordEncoder.encode(UUID.randomUUID().toString()));
                    usuario.setEstado(true);
                    usuario.setFechaRegistro(LocalDateTime.now());
                    usuario.setTipoDocumento(tipoDocumento);
                    usuario.setRoles(Set.of(rol));

                    _usuarioRepository.save(usuario);

                    vistosCorreoArchivo.add(correo);
                    vistosDocArchivo.add(numeroDocumento);
                    correosEnBd.add(correo);
                    docsEnBd.add(numeroDocumento);
                    exitosos++;
                } catch (NumberFormatException ex) {
                    errores.add("Fila " + numeroFila + ": idTipoDocumento o idRol no numérico");
                } catch (RecursoNoEncontradoException | OperacionNoPermitidaException ex) {
                    errores.add("Fila " + numeroFila + ": " + ex.getMessage());
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("No se pudo leer el CSV: " + e.getMessage(), e);
        }

        return ImportacionCsvResultadoDTO.builder()
                .filasExitosas(exitosos)
                .filasConError(errores.size())
                .erroresPorFila(errores)
                .build();
    }

    private void validarPoliticaAdministradoresAlCrear(Rol nuevoRol) {
        long adminsActuales = _usuarioRepository.countUsuariosConRol("ROLE_ADMIN");
        boolean seraAdmin = "ROLE_ADMIN".equals(nuevoRol.getNombre());
        if (adminsActuales + (seraAdmin ? 1 : 0) < 1) {
            throw new OperacionNoPermitidaException("Debe existir al menos un usuario administrador en el sistema.");
        }
    }

    private void validarPoliticaAdministradoresTrasCambioRol(Long idUsuario, Rol nuevoRol) {
        boolean seraAdmin = "ROLE_ADMIN".equals(nuevoRol.getNombre());
        long otrosAdmins = _usuarioRepository.countUsuariosConRolExceptoUsuario("ROLE_ADMIN", idUsuario);
        long administradoresTrasCambio = otrosAdmins + (seraAdmin ? 1 : 0);
        if (administradoresTrasCambio < 1) {
            throw new OperacionNoPermitidaException("Debe existir al menos un usuario administrador en el sistema.");
        }
    }

    private UsuarioDTO mapearUsuarioDTO(Usuario usuario) {
        UsuarioDTO dto = _modelMapper.map(usuario, UsuarioDTO.class);
        dto.setIdTipoDocumento(usuario.getTipoDocumento().getIdTipoDocumento());

        Rol rolPrincipal = usuario.getRoles().stream().findFirst().orElse(null);
        if (rolPrincipal != null) {
            dto.setIdRol(rolPrincipal.getIdRol());
            dto.setRol(toRolResumen(rolPrincipal));
        }

        TipoDocumento td = usuario.getTipoDocumento();
        if (td != null) {
            dto.setTipoDocumento(new TipoDocumentoResumenDTO(td.getIdTipoDocumento(), td.getNombre()));
        }

        return dto;
    }

    private static RolResumenDTO toRolResumen(Rol rol) {
        return new RolResumenDTO(rol.getIdRol(), rol.getNombre(), codigoSinPrefijoRol(rol.getNombre()));
    }

    private static String codigoSinPrefijoRol(String nombreRol) {
        if (nombreRol == null) {
            return null;
        }
        return nombreRol.startsWith("ROLE_") ? nombreRol.substring(5) : nombreRol;
    }

    private String obtenerRolPrincipal(Usuario usuario) {
        return usuario.getRoles().stream()
                .findFirst()
                .map(Rol::getNombre)
                .orElse("ROLE_ESTUDIANTE");
    }

    @Override
    @Transactional
    public UsuarioDTO actualizarMiTelefono(String telefono) {
        if (telefono == null || telefono.isBlank()) {
            throw new OperacionNoPermitidaException("El teléfono es obligatorio.");
        }
        String limpio = telefono.trim();
        if (limpio.length() < 7 || limpio.length() > 20) {
            throw new OperacionNoPermitidaException("Ingrese un teléfono válido (7 a 20 caracteres).");
        }
        Usuario usuario = _securityUsuarioHelper.usuarioAutenticado();
        usuario.setTelefono(limpio);
        usuario = _usuarioRepository.save(usuario);
        UsuarioDTO dto = mapearUsuarioDTO(usuario);
        return dto;
    }
}
