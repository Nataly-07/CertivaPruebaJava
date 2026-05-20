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

import com.certiva.api.DTO.CrearTipoDocumentoDTO;
import com.certiva.api.DTO.TipoDocumentoDTO;
import com.certiva.api.Service.TipoDocumentoService;

@RestController
@RequestMapping("/api/tipo-documentos")
public class TipoDocumentoController {

    private final TipoDocumentoService _tipoDocumentoService;

    public TipoDocumentoController(TipoDocumentoService tipoDocumentoService) {
        this._tipoDocumentoService = tipoDocumentoService;
    }

    @PostMapping
    public ResponseEntity<CrearTipoDocumentoDTO> crearTipoDocumento(@RequestBody CrearTipoDocumentoDTO tipoDocumentoDTO) {
        CrearTipoDocumentoDTO nuevo = _tipoDocumentoService.crearTipoDocumento(tipoDocumentoDTO);
        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TipoDocumentoDTO>> listarTipoDocumentos() {
        List<TipoDocumentoDTO> listado = _tipoDocumentoService.listarTipoDocumentos();
        return ResponseEntity.ok(listado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TipoDocumentoDTO> actualizarTipoDocumento(@PathVariable Long id, @RequestBody TipoDocumentoDTO tipoDocumentoDTO) {
        tipoDocumentoDTO.setIdTipoDocumento(id);
        TipoDocumentoDTO actualizado = _tipoDocumentoService.actualizarTipoDocumento(tipoDocumentoDTO);
        return ResponseEntity.ok(actualizado);
    }

    @PutMapping("/{id}/inactivar")
    public ResponseEntity<String> inactivarTipoDocumento(@PathVariable Long id) {
        String mensaje = _tipoDocumentoService.inactivarTipoDocumento(id);
        return ResponseEntity.ok(mensaje);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> borrarTipoDocumento(@PathVariable Long id) {
        boolean eliminado = _tipoDocumentoService.borrarTipoDocumento(id);
        return ResponseEntity.ok(eliminado);
    }
}
