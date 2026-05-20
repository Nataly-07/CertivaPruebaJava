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

import com.certiva.api.DTO.CrearResultadoEventoDTO;
import com.certiva.api.DTO.ResultadoEventoDTO;
import com.certiva.api.Service.ResultadoEventoService;

@RestController
@RequestMapping("/api/resultado-eventos")
public class ResultadoEventoController {

    private final ResultadoEventoService _resultadoEventoService;

    public ResultadoEventoController(ResultadoEventoService resultadoEventoService) {
        this._resultadoEventoService = resultadoEventoService;
    }

    @PostMapping
    public ResponseEntity<CrearResultadoEventoDTO> crearResultadoEvento(@RequestBody CrearResultadoEventoDTO resultadoDTO) {
        CrearResultadoEventoDTO nuevo = _resultadoEventoService.crearResultadoEvento(resultadoDTO);
        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ResultadoEventoDTO>> listarResultadoEventos() {
        List<ResultadoEventoDTO> listado = _resultadoEventoService.listarResultadoEventos();
        return ResponseEntity.ok(listado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResultadoEventoDTO> actualizarResultadoEvento(@PathVariable Long id, @RequestBody ResultadoEventoDTO resultadoDTO) {
        resultadoDTO.setId(id);
        ResultadoEventoDTO actualizado = _resultadoEventoService.actualizarResultadoEvento(resultadoDTO);
        return ResponseEntity.ok(actualizado);
    }

    @PutMapping("/{id}/inactivar")
    public ResponseEntity<String> inactivarResultadoEvento(@PathVariable Long id) {
        String mensaje = _resultadoEventoService.inactivarResultadoEvento(id);
        return ResponseEntity.ok(mensaje);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> borrarResultadoEvento(@PathVariable Long id) {
        boolean eliminado = _resultadoEventoService.borrarResultadoEvento(id);
        return ResponseEntity.ok(eliminado);
    }
}
