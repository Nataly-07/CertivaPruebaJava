package com.certiva.api.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.certiva.api.DTO.CheckInRequestDTO;
import com.certiva.api.DTO.CheckInRespuestaDTO;
import com.certiva.api.DTO.CrearInscripcionDTO;
import com.certiva.api.DTO.InscripcionDTO;
import com.certiva.api.DTO.InscripcionPortalDTO;
import com.certiva.api.Service.InscripcionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/inscripciones")
public class InscripcionController {

    private final InscripcionService _inscripcionService;

    public InscripcionController(InscripcionService inscripcionService) {
        this._inscripcionService = inscripcionService;
    }

    @PostMapping
    public ResponseEntity<CrearInscripcionDTO> crearInscripcion(@Valid @RequestBody CrearInscripcionDTO inscripcionDTO) {
        CrearInscripcionDTO nuevo = _inscripcionService.crearInscripcion(inscripcionDTO);
        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }

    @PostMapping("/confirmar-qr")
    public ResponseEntity<CheckInRespuestaDTO> confirmarQr(@Valid @RequestBody CheckInRequestDTO body) {
        return ResponseEntity.ok(_inscripcionService.confirmarAsistenciaPorCodigoQr(body.getCodigo()));
    }

    @GetMapping("/mis")
    public ResponseEntity<List<InscripcionPortalDTO>> listarMisInscripciones() {
        return ResponseEntity.ok(_inscripcionService.listarMisInscripciones());
    }

    @GetMapping
    public ResponseEntity<List<InscripcionDTO>> listarInscripciones() {
        List<InscripcionDTO> listado = _inscripcionService.listarInscripciones();
        return ResponseEntity.ok(listado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InscripcionDTO> actualizarInscripcion(@PathVariable Long id, @RequestBody InscripcionDTO inscripcionDTO) {
        inscripcionDTO.setIdInscripcion(id);
        InscripcionDTO actualizado = _inscripcionService.actualizarInscripcion(inscripcionDTO);
        return ResponseEntity.ok(actualizado);
    }

    @PutMapping("/{id}/inactivar")
    public ResponseEntity<String> inactivarInscripcion(@PathVariable Long id) {
        String mensaje = _inscripcionService.inactivarInscripcion(id);
        return ResponseEntity.ok(mensaje);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> borrarInscripcion(@PathVariable Long id) {
        boolean eliminado = _inscripcionService.borrarInscripcion(id);
        return ResponseEntity.ok(eliminado);
    }
}
