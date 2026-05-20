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

import com.certiva.api.DTO.CrearPagoDTO;
import com.certiva.api.DTO.PagoDTO;
import com.certiva.api.Service.PagoService;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService _pagoService;

    public PagoController(PagoService pagoService) {
        this._pagoService = pagoService;
    }

    @PostMapping
    public ResponseEntity<CrearPagoDTO> crearPago(@RequestBody CrearPagoDTO pagoDTO) {
        CrearPagoDTO nuevo = _pagoService.crearPago(pagoDTO);
        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PagoDTO>> listarPagos() {
        List<PagoDTO> listado = _pagoService.listarPagos();
        return ResponseEntity.ok(listado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PagoDTO> actualizarPago(@PathVariable Long id, @RequestBody PagoDTO pagoDTO) {
        pagoDTO.setIdPago(id);
        PagoDTO actualizado = _pagoService.actualizarPago(pagoDTO);
        return ResponseEntity.ok(actualizado);
    }

    @PutMapping("/{id}/inactivar")
    public ResponseEntity<String> inactivarPago(@PathVariable Long id) {
        String mensaje = _pagoService.inactivarPago(id);
        return ResponseEntity.ok(mensaje);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> borrarPago(@PathVariable Long id) {
        boolean eliminado = _pagoService.borrarPago(id);
        return ResponseEntity.ok(eliminado);
    }
}
