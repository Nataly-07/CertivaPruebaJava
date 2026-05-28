package com.certiva.api.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.certiva.api.DTO.AsistenciaManualRequestDTO;
import com.certiva.api.DTO.CheckInRespuestaDTO;
import com.certiva.api.DTO.EventoAsistenciaEnVivoDTO;
import com.certiva.api.DTO.EventoCierreResultadoDTO;
import com.certiva.api.DTO.EventoDTO;
import com.certiva.api.Service.EventoCicloVidaService;
import com.certiva.api.Service.EventoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class EventsApiController {

    private final EventoService eventoService;
    private final EventoCicloVidaService eventoCicloVidaService;

    public EventsApiController(EventoService eventoService, EventoCicloVidaService eventoCicloVidaService) {
        this.eventoService = eventoService;
        this.eventoCicloVidaService = eventoCicloVidaService;
    }

    @GetMapping("/events")
    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    public ResponseEntity<List<EventoDTO>> getAllEvents() {
        return ResponseEntity.ok(eventoService.listarEventos(null, null, null, null, null));
    }

    @GetMapping("/events/{eventId}/attendance-matrix")
    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    public ResponseEntity<EventoAsistenciaEnVivoDTO> getAttendanceMatrix(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventoService.obtenerAsistenciaEnVivo(eventId));
    }

    @PostMapping("/attendance/manual-checkin")
    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    public ResponseEntity<CheckInRespuestaDTO> manualCheckin(@Valid @RequestBody AsistenciaManualRequestDTO request) {
        return ResponseEntity.ok(eventoService.registrarAsistenciaManual(request));
    }

    @PostMapping("/events/{eventId}/close")
    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    public ResponseEntity<EventoCierreResultadoDTO> closeEvent(@PathVariable Long eventId) {
        EventoCierreResultadoDTO result = eventoCicloVidaService.cerrarEventoYCertificar(eventId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(result);
    }
}
