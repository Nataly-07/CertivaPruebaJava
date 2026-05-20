package com.certiva.api.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.certiva.api.DTO.AuditoriaResumenDTO;
import com.certiva.api.Service.AuditoriaService;

@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping("/recientes")
    public ResponseEntity<List<AuditoriaResumenDTO>> ultimas(
            @RequestParam(name = "limite", defaultValue = "100") int limite) {
        return ResponseEntity.ok(auditoriaService.listarUltimasAuditorias(limite));
    }
}
