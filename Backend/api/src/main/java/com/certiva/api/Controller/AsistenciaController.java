package com.certiva.api.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.certiva.api.DTO.CheckInRespuestaDTO;
import com.certiva.api.Service.InscripcionService;

@RestController
@RequestMapping("/api/asistencias")
public class AsistenciaController {

    private final InscripcionService inscripcionService;

    public AsistenciaController(InscripcionService inscripcionService) {
        this.inscripcionService = inscripcionService;
    }

    /**
     * Registra asistencia a partir del id de inscripción escaneado en el QR.
     * Requiere rol MONITOR o ADMIN (JWT).
     */
    @PostMapping("/validar")
    public ResponseEntity<CheckInRespuestaDTO> validar(
            @RequestParam("inscripcionId") Long inscripcionId) {
        return ResponseEntity.ok(
                inscripcionService.confirmarAsistenciaPorCodigoQr(String.valueOf(inscripcionId)));
    }
}
