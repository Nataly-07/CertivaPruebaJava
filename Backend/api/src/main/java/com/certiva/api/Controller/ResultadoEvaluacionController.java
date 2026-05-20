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

import com.certiva.api.DTO.CrearResultadoEvaluacionDTO;
import com.certiva.api.DTO.ResultadoEvaluacionDTO;
import com.certiva.api.Service.ResultadoEvaluacionService;

@RestController
@RequestMapping("/api/resultado-evaluaciones")
public class ResultadoEvaluacionController {

    private final ResultadoEvaluacionService _resultadoEvaluacionService;

    public ResultadoEvaluacionController(ResultadoEvaluacionService resultadoEvaluacionService) {
        this._resultadoEvaluacionService = resultadoEvaluacionService;
    }

    @PostMapping
    public ResponseEntity<CrearResultadoEvaluacionDTO> crearResultadoEvaluacion(@RequestBody CrearResultadoEvaluacionDTO resultadoDTO) {
        CrearResultadoEvaluacionDTO nuevo = _resultadoEvaluacionService.crearResultadoEvaluacion(resultadoDTO);
        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ResultadoEvaluacionDTO>> listarResultadoEvaluaciones() {
        List<ResultadoEvaluacionDTO> listado = _resultadoEvaluacionService.listarResultadoEvaluaciones();
        return ResponseEntity.ok(listado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResultadoEvaluacionDTO> actualizarResultadoEvaluacion(@PathVariable Long id, @RequestBody ResultadoEvaluacionDTO resultadoDTO) {
        resultadoDTO.setId(id);
        ResultadoEvaluacionDTO actualizado = _resultadoEvaluacionService.actualizarResultadoEvaluacion(resultadoDTO);
        return ResponseEntity.ok(actualizado);
    }

    @PutMapping("/{id}/inactivar")
    public ResponseEntity<String> inactivarResultadoEvaluacion(@PathVariable Long id) {
        String mensaje = _resultadoEvaluacionService.inactivarResultadoEvaluacion(id);
        return ResponseEntity.ok(mensaje);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> borrarResultadoEvaluacion(@PathVariable Long id) {
        boolean eliminado = _resultadoEvaluacionService.borrarResultadoEvaluacion(id);
        return ResponseEntity.ok(eliminado);
    }
}
