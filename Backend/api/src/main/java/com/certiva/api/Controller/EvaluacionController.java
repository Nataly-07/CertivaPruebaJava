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

import com.certiva.api.DTO.CrearEvaluacionDTO;
import com.certiva.api.DTO.EvaluacionDTO;
import com.certiva.api.Service.EvaluacionService;

@RestController
@RequestMapping("/api/evaluaciones")
public class EvaluacionController {

    private final EvaluacionService _evaluacionService;

    public EvaluacionController(EvaluacionService evaluacionService) {
        this._evaluacionService = evaluacionService;
    }

    @PostMapping
    public ResponseEntity<CrearEvaluacionDTO> crearEvaluacion(@RequestBody CrearEvaluacionDTO evaluacionDTO) {
        CrearEvaluacionDTO nuevo = _evaluacionService.crearEvaluacion(evaluacionDTO);
        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<EvaluacionDTO>> listarEvaluaciones() {
        List<EvaluacionDTO> listado = _evaluacionService.listarEvaluaciones();
        return ResponseEntity.ok(listado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EvaluacionDTO> actualizarEvaluacion(@PathVariable Long id, @RequestBody EvaluacionDTO evaluacionDTO) {
        evaluacionDTO.setIdEvaluacion(id);
        EvaluacionDTO actualizado = _evaluacionService.actualizarEvaluacion(evaluacionDTO);
        return ResponseEntity.ok(actualizado);
    }

    @PutMapping("/{id}/inactivar")
    public ResponseEntity<String> inactivarEvaluacion(@PathVariable Long id) {
        String mensaje = _evaluacionService.inactivarEvaluacion(id);
        return ResponseEntity.ok(mensaje);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> borrarEvaluacion(@PathVariable Long id) {
        boolean eliminado = _evaluacionService.borrarEvaluacion(id);
        return ResponseEntity.ok(eliminado);
    }
}
