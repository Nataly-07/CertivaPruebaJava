package com.certiva.api.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.certiva.api.DTO.ActualizarTelefonoDTO;
import com.certiva.api.DTO.CambiarRolDTO;
import com.certiva.api.DTO.CrearUsuarioDTO;
import com.certiva.api.DTO.ImportacionCsvResultadoDTO;
import com.certiva.api.DTO.LoginDTO;
import com.certiva.api.DTO.LoginRespuestaDTO;
import com.certiva.api.DTO.UsuarioDTO;
import com.certiva.api.Service.UsuarioService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService _usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this._usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginRespuestaDTO> login(@Valid @RequestBody LoginDTO loginDTO,
                                                    HttpServletRequest request) {
        String ip = resolverIpCliente(request);
        LoginRespuestaDTO respuesta = _usuarioService.login(loginDTO, ip);
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/registrar")
    public ResponseEntity<CrearUsuarioDTO> registrar(@Valid @RequestBody CrearUsuarioDTO usuarioDTO) {
        CrearUsuarioDTO nuevo = _usuarioService.registrarUsuario(usuarioDTO);
        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }

    @PostMapping
    public ResponseEntity<CrearUsuarioDTO> crearDesdeAdministracion(@Valid @RequestBody CrearUsuarioDTO usuarioDTO) {
        CrearUsuarioDTO nuevo = _usuarioService.crearUsuarioAdministracion(usuarioDTO);
        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }

    @PostMapping(value = "/importar-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportacionCsvResultadoDTO> importarCsv(@RequestParam("archivo") MultipartFile archivo) {
        ImportacionCsvResultadoDTO resultado = _usuarioService.importarUsuariosDesdeCsv(archivo);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> listarUsuarios() {
        List<UsuarioDTO> listado = _usuarioService.listarUsuarios();
        return ResponseEntity.ok(listado);
    }

    @GetMapping("/rol/{codigoRol}")
    public ResponseEntity<List<com.certiva.api.DTO.UsuarioStaffDTO>> listarPorRol(
            @PathVariable String codigoRol,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(_usuarioService.listarStaffPorRol(codigoRol, q));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(_usuarioService.obtenerUsuarioPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> actualizarUsuario(@PathVariable Long id,
                                                         @Valid @RequestBody UsuarioDTO usuarioDTO) {
        usuarioDTO.setIdUsuario(id);
        UsuarioDTO actualizado = _usuarioService.actualizarUsuario(usuarioDTO);
        return ResponseEntity.ok(actualizado);
    }

    @PutMapping("/{id}/inactivar")
    public ResponseEntity<String> inactivarUsuario(@PathVariable Long id) {
        String mensaje = _usuarioService.inactivarUsuario(id);
        return ResponseEntity.ok(mensaje);
    }

    @PatchMapping("/{id}/rol")
    public ResponseEntity<UsuarioDTO> cambiarRol(@PathVariable Long id,
                                                 @Valid @RequestBody CambiarRolDTO body) {
        UsuarioDTO actualizado = _usuarioService.cambiarRolUsuario(id, body.getIdRol());
        return ResponseEntity.ok(actualizado);
    }

    @PatchMapping("/mi-perfil/telefono")
    public ResponseEntity<UsuarioDTO> actualizarMiTelefono(@Valid @RequestBody ActualizarTelefonoDTO body) {
        return ResponseEntity.ok(_usuarioService.actualizarMiTelefono(body.getTelefono()));
    }

    private static String resolverIpCliente(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
