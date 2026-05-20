package com.certiva.api.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.certiva.api.DTO.Rol.CrearRolDTO;
import com.certiva.api.DTO.Rol.RolDTO;
import com.certiva.api.DTO.RolResumenDTO;
import com.certiva.api.Service.RolService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/roles")
public class RolController {

    private final RolService _rolService;

    public RolController(RolService rolService) {
        this._rolService = rolService;
    }

    @PostMapping
    public ResponseEntity<RolDTO> crearRol(@Valid @RequestBody CrearRolDTO rolDTO) {
        RolDTO nuevoRol = _rolService.crearRol(rolDTO);
        return new ResponseEntity<>(nuevoRol, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<RolDTO>> listarRolesActivos() {
        List<RolDTO> roles = _rolService.listarRolesActivos();
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/listar")
    public ResponseEntity<List<RolResumenDTO>> listarRolesFormularioAdministracion() {
        return ResponseEntity.ok(_rolService.listarRolesParaAdministracion());
    }

    @GetMapping("/registro")
    public ResponseEntity<List<RolResumenDTO>> listarRolRegistroPublico() {
        return ResponseEntity.ok(_rolService.listarRolesParaRegistroEstudiante());
    }

    @GetMapping("/todos")
    public ResponseEntity<List<RolDTO>> listarTodosLosRoles() {
        List<RolDTO> roles = _rolService.listarRoles();
        return ResponseEntity.ok(roles);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RolDTO> actualizarRol(@PathVariable Long id,
                                                 @Valid @RequestBody CrearRolDTO rolDTO) {
        RolDTO rolActualizado = _rolService.actualizarRol(id, rolDTO);
        return ResponseEntity.ok(rolActualizado);
    }

    @PutMapping("/{id}/inactivar")
    public ResponseEntity<String> inactivarRol(@PathVariable Long id) {
        String mensaje = _rolService.inactivarRol(id);
        return ResponseEntity.ok(mensaje);
    }
}
