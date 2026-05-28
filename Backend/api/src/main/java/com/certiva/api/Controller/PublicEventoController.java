package com.certiva.api.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import com.certiva.api.DTO.EventoCatalogoPublicoDTO;
import com.certiva.api.DTO.EventoCupoVerificacionDTO;
import com.certiva.api.DTO.EventoPublicoDTO;
import com.certiva.api.Service.EventoService;

@RestController
@RequestMapping("/api/public/eventos")
@CrossOrigin(origins = { "http://localhost:4200", "http://127.0.0.1:4200" })
public class PublicEventoController {

    private static final Logger log = LoggerFactory.getLogger(PublicEventoController.class);

    private final EventoService eventoService;

    public PublicEventoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @GetMapping("/catalogo")
    public ResponseEntity<List<EventoCatalogoPublicoDTO>> listarCatalogo() {
        try {
            return ResponseEntity.ok(eventoService.listarCatalogoPublico());
        } catch (Exception ex) {
            log.error("Fallo catálogo público", ex);
            return ResponseEntity.ok(List.of());
        }
    }

    @GetMapping("/{idEvento}")
    public ResponseEntity<EventoPublicoDTO> obtenerPorId(@PathVariable Long idEvento) {
        return ResponseEntity.ok(eventoService.obtenerPublicoPorId(idEvento));
    }

    @GetMapping("/{idEvento}/cupo")
    public ResponseEntity<EventoCupoVerificacionDTO> verificarCupoPorId(@PathVariable Long idEvento) {
        return ResponseEntity.ok(eventoService.verificarCupo(idEvento));
    }

    @GetMapping("/difusion/{codigoDifusion}")
    public ResponseEntity<EventoPublicoDTO> obtenerPorCodigoDifusion(@PathVariable String codigoDifusion) {
        return ResponseEntity.ok(eventoService.obtenerPublicoPorCodigoDifusion(codigoDifusion));
    }

    @GetMapping("/difusion/{codigoDifusion}/cupo")
    public ResponseEntity<EventoCupoVerificacionDTO> verificarCupoPorDifusion(@PathVariable String codigoDifusion) {
        EventoPublicoDTO ev = eventoService.obtenerPublicoPorCodigoDifusion(codigoDifusion);
        return ResponseEntity.ok(eventoService.verificarCupo(ev.getIdEvento()));
    }
}
