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

import com.certiva.api.DTO.CrearContenidoCursoDTO;
import com.certiva.api.DTO.ContenidoCursoDTO;
import com.certiva.api.Service.ContenidoCursoService;

@RestController
@RequestMapping("/api/contenido-cursos")
public class ContenidoCursoController {

    private final ContenidoCursoService _contenidoCursoService;

    public ContenidoCursoController(ContenidoCursoService contenidoCursoService) {
        this._contenidoCursoService = contenidoCursoService;
    }

    @PostMapping
    public ResponseEntity<CrearContenidoCursoDTO> crearContenidoCurso(@RequestBody CrearContenidoCursoDTO contenidoDTO) {
        CrearContenidoCursoDTO nuevo = _contenidoCursoService.crearContenidoCurso(contenidoDTO);
        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ContenidoCursoDTO>> listarContenidoCursos() {
        List<ContenidoCursoDTO> listado = _contenidoCursoService.listarContenidoCursos();
        return ResponseEntity.ok(listado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContenidoCursoDTO> actualizarContenidoCurso(@PathVariable Long id, @RequestBody ContenidoCursoDTO contenidoDTO) {
        contenidoDTO.setIdContenido(id);
        ContenidoCursoDTO actualizado = _contenidoCursoService.actualizarContenidoCurso(contenidoDTO);
        return ResponseEntity.ok(actualizado);
    }

    @PutMapping("/{id}/inactivar")
    public ResponseEntity<String> inactivarContenidoCurso(@PathVariable Long id) {
        String mensaje = _contenidoCursoService.inactivarContenidoCurso(id);
        return ResponseEntity.ok(mensaje);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> borrarContenidoCurso(@PathVariable Long id) {
        boolean eliminado = _contenidoCursoService.borrarContenidoCurso(id);
        return ResponseEntity.ok(eliminado);
    }
}
