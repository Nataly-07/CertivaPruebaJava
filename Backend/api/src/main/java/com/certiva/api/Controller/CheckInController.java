package com.certiva.api.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.certiva.api.DTO.CheckInRequestDTO;
import com.certiva.api.DTO.CheckInRespuestaDTO;
import com.certiva.api.Service.InscripcionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class CheckInController {

    private final InscripcionService inscripcionService;

    public CheckInController(InscripcionService inscripcionService) {
        this.inscripcionService = inscripcionService;
    }

    @PostMapping("/check-in")
    public ResponseEntity<CheckInRespuestaDTO> checkIn(@Valid @RequestBody CheckInRequestDTO body) {
        return ResponseEntity.ok(
                inscripcionService.confirmarAsistenciaPorCodigoQr(body.getCodigo(), body.getTipoAsistencia()));
    }
}
