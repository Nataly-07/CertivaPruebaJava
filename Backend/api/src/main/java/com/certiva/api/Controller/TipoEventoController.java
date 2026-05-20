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

import com.certiva.api.DTO.CrearTipoEventoDTO;
import com.certiva.api.DTO.TipoEventoDTO;
import com.certiva.api.Service.TipoEventoService;

@RestController
@RequestMapping("/api/tipo-eventos")
public class TipoEventoController {

    private final TipoEventoService _tipoEventoService;

    public TipoEventoController(TipoEventoService tipoEventoService) {
        this._tipoEventoService = tipoEventoService;
    }

    @PostMapping
    public ResponseEntity<CrearTipoEventoDTO> crearTipoEvento(@RequestBody CrearTipoEventoDTO tipoEventoDTO) {
        CrearTipoEventoDTO nuevo = _tipoEventoService.crearTipoEvento(tipoEventoDTO);
        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TipoEventoDTO>> listarTipoEventos() {
        List<TipoEventoDTO> listado = _tipoEventoService.listarTipoEventos();
        return ResponseEntity.ok(listado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TipoEventoDTO> actualizarTipoEvento(@PathVariable Long id, @RequestBody TipoEventoDTO tipoEventoDTO) {
        tipoEventoDTO.setIdTipoEvento(id);
        TipoEventoDTO actualizado = _tipoEventoService.actualizarTipoEvento(tipoEventoDTO);
        return ResponseEntity.ok(actualizado);
    }

    @PutMapping("/{id}/inactivar")
    public ResponseEntity<String> inactivarTipoEvento(@PathVariable Long id) {
        String mensaje = _tipoEventoService.inactivarTipoEvento(id);
        return ResponseEntity.ok(mensaje);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> borrarTipoEvento(@PathVariable Long id) {
        boolean eliminado = _tipoEventoService.borrarTipoEvento(id);
        return ResponseEntity.ok(eliminado);
    }
}
