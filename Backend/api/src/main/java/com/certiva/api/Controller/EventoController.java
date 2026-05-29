package com.certiva.api.Controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
import com.certiva.api.DTO.EventoCierreResultadoDTO;
import com.certiva.api.DTO.EventoRevisionPanelDTO;
import com.certiva.api.DTO.EventoAsistenciaEnVivoDTO;
import com.certiva.api.DTO.EventoContenidoAcademicoDTO;
import com.certiva.api.DTO.GuardarEventoContenidoAcademicoDTO;
import com.certiva.api.DTO.GuardarRevisionEvaluacionesDTO;
import com.certiva.api.DTO.ProfesorParticipanteDTO;
import com.certiva.api.DTO.MonitorPanelDTO;
import com.certiva.api.DTO.ProfesorPanelDTO;
import com.certiva.api.DTO.ReasignarStaffDTO;
import com.certiva.api.Service.EventoCicloVidaService;
import com.certiva.api.Service.EventoService;
import com.certiva.api.enums.EstadoOperativoEvento;
import com.certiva.api.enums.ModalidadEvento;
import com.certiva.api.enums.TipoEventoEnum;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/eventos")
public class EventoController {

    private final EventoService _eventoService;
    private final EventoCicloVidaService _eventoCicloVidaService;

    public EventoController(EventoService eventoService, EventoCicloVidaService eventoCicloVidaService) {
        this._eventoService = eventoService;
        this._eventoCicloVidaService = eventoCicloVidaService;
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

    @GetMapping("/mi-panel-monitor")
    public ResponseEntity<MonitorPanelDTO> panelMonitor() {
        return ResponseEntity.ok(_eventoService.obtenerPanelMonitor());
    }

    @GetMapping("/mi-panel/revision/{id}")
    public ResponseEntity<EventoRevisionPanelDTO> revisionCierre(@PathVariable Long id) {
        return ResponseEntity.ok(_eventoService.obtenerRevisionCierre(id));
    }

    @GetMapping("/mi-panel/{id}/asistencia-en-vivo")
    public ResponseEntity<EventoAsistenciaEnVivoDTO> asistenciaEnVivo(@PathVariable Long id) {
        return ResponseEntity.ok(_eventoService.obtenerAsistenciaEnVivo(id));
    }

    @PutMapping("/mi-panel/revision/{id}/evaluaciones")
    public ResponseEntity<EventoRevisionPanelDTO> guardarEvaluacionesRevision(
            @PathVariable Long id,
            @RequestBody GuardarRevisionEvaluacionesDTO body) {
        return ResponseEntity.ok(_eventoService.guardarEvaluacionesRevision(id, body));
    }

    @GetMapping("/mi-panel/{id}/contenido-academico")
    public ResponseEntity<EventoContenidoAcademicoDTO> contenidoAcademico(@PathVariable Long id) {
        return ResponseEntity.ok(_eventoService.obtenerContenidoAcademico(id));
    }

    @PutMapping("/mi-panel/{id}/contenido-academico")
    public ResponseEntity<EventoContenidoAcademicoDTO> guardarContenidoAcademico(
            @PathVariable Long id,
            @RequestBody GuardarEventoContenidoAcademicoDTO body) {
        return ResponseEntity.ok(_eventoService.guardarContenidoAcademico(id, body));
    }

    @GetMapping("/mi-panel/{id}/participantes")
    public ResponseEntity<List<ProfesorParticipanteDTO>> participantesAsignados(@PathVariable Long id) {
        return ResponseEntity.ok(_eventoService.listarParticipantesAsignados(id));
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
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(required = false) EstadoOperativoEvento estadoOperativo) {
        return ResponseEntity.ok(_eventoService.listarVistaAdmin(soloActivos, modalidad, tipo, desde, hasta, estadoOperativo));
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

    @PatchMapping("/{id}/reasignar-staff")
    public ResponseEntity<EventoDTO> reasignarStaff(@PathVariable Long id, @RequestBody ReasignarStaffDTO dto) {
        return ResponseEntity.ok(_eventoService.reasignarStaff(id, dto));
    }

    @PutMapping("/{id}/inactivar")
    public ResponseEntity<String> inactivarEvento(@PathVariable Long id) {
        return ResponseEntity.ok(_eventoService.inactivarEvento(id));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<String> cancelarEvento(@PathVariable Long id) {
        _eventoService.cancelarEvento(id);
        return ResponseEntity.ok("Evento cancelado correctamente");
    }

    @PostMapping("/{id}/iniciar-revision")
    public ResponseEntity<String> iniciarRevision(@PathVariable Long id) {
        _eventoCicloVidaService.iniciarRevision(id);
        return ResponseEntity.ok("Revisión académica iniciada");
    }

    @PostMapping("/{id}/cerrar-y-certificar")
    public ResponseEntity<EventoCierreResultadoDTO> cerrarYCertificar(@PathVariable Long id) {
        return ResponseEntity.ok(_eventoCicloVidaService.cerrarEventoYCertificar(id));
    }

    /** Alias semántico para clausura inmutable desde el panel del profesor. */
    @PostMapping("/{id}/clausurar")
    public ResponseEntity<EventoCierreResultadoDTO> clausurar(@PathVariable Long id) {
        return ResponseEntity.ok(_eventoCicloVidaService.cerrarEventoYCertificar(id));
    }

    @PostMapping("/{id}/forzar-cierre")
    public ResponseEntity<EventoCierreResultadoDTO> forzarCierre(@PathVariable Long id) {
        return ResponseEntity.ok(_eventoCicloVidaService.forzarCierreAdministrador(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> borrarEvento(@PathVariable Long id) {
        _eventoService.borrarEventoLogico(id);
        return ResponseEntity.ok("Evento cancelado (soft delete)");
    }
}
