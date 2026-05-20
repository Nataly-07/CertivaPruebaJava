package com.certiva.api.Controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.certiva.api.DTO.CampoFormularioDTO;
import com.certiva.api.DTO.CrearEventoDTO;
import com.certiva.api.DTO.EventoCupoVerificacionDTO;
import com.certiva.api.DTO.EventoDTO;
import com.certiva.api.DTO.EventoFilaAdminDTO;
import com.certiva.api.DTO.EventoResumenTipoDTO;
import com.certiva.api.DTO.ProfesorPanelDTO;
import com.certiva.api.Service.EventoService;
import com.certiva.api.enums.ModalidadEvento;
import com.certiva.api.enums.TipoEventoEnum;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/eventos")
public class EventoController {

    private final EventoService _eventoService;

    public EventoController(EventoService eventoService) {
        this._eventoService = eventoService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EventoDTO> crearEvento(
            @RequestPart("evento") @Valid CrearEventoDTO eventoDTO,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen,
            @RequestPart(value = "pensum", required = false) MultipartFile pensum) {
        EventoDTO creado = _eventoService.crearEvento(eventoDTO, imagen, pensum);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @GetMapping("/mi-panel")
    public ResponseEntity<ProfesorPanelDTO> panelProfesor() {
        return ResponseEntity.ok(_eventoService.obtenerPanelProfesor());
    }

    @GetMapping("/resumen-tipos")
    public ResponseEntity<List<EventoResumenTipoDTO>> resumenTipos(
            @RequestParam(required = false) Boolean soloActivos,
            @RequestParam(required = false) ModalidadEvento modalidad) {
        return ResponseEntity.ok(_eventoService.listarResumenTipos(soloActivos, modalidad));
    }

    @GetMapping("/vista-admin")
    public ResponseEntity<List<EventoFilaAdminDTO>> vistaAdmin(
            @RequestParam(required = false) Boolean soloActivos,
            @RequestParam(required = false) ModalidadEvento modalidad,
            @RequestParam(required = false) TipoEventoEnum tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return ResponseEntity.ok(_eventoService.listarVistaAdmin(soloActivos, modalidad, tipo, desde, hasta));
    }

    @GetMapping
    public ResponseEntity<List<EventoDTO>> listarEventos(
            @RequestParam(required = false) Boolean soloActivos,
            @RequestParam(required = false) ModalidadEvento modalidad,
            @RequestParam(required = false) TipoEventoEnum tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return ResponseEntity.ok(_eventoService.listarEventos(soloActivos, modalidad, tipo, desde, hasta));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventoDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(_eventoService.obtenerPorId(id));
    }

    @GetMapping("/{id}/verificar-cupo")
    public ResponseEntity<EventoCupoVerificacionDTO> verificarCupo(@PathVariable Long id) {
        return ResponseEntity.ok(_eventoService.verificarCupo(id));
    }

    @GetMapping("/{id}/campos")
    public ResponseEntity<List<CampoFormularioDTO>> listarCamposPersonalizados(@PathVariable Long id) {
        return ResponseEntity.ok(_eventoService.listarCamposPorEvento(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventoDTO> actualizarEvento(@PathVariable Long id, @Valid @RequestBody EventoDTO eventoDTO) {
        eventoDTO.setIdEvento(id);
        return ResponseEntity.ok(_eventoService.actualizarEvento(eventoDTO));
    }

    @PutMapping("/{id}/inactivar")
    public ResponseEntity<String> inactivarEvento(@PathVariable Long id) {
        return ResponseEntity.ok(_eventoService.inactivarEvento(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> borrarEvento(@PathVariable Long id) {
        _eventoService.borrarEventoLogico(id);
        return ResponseEntity.ok("Evento desactivado correctamente");
    }
}
