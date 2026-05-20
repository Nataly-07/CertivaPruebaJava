package com.certiva.api.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.certiva.api.DTO.DashboardActivityDTO;
import com.certiva.api.DTO.DashboardDTO;
import com.certiva.api.Service.DashboardService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardDTO> estadisticas(HttpServletRequest request) {
        return ResponseEntity.ok(dashboardService.obtenerEstadisticas(resolveClientIp(request)));
    }

    /**
     * @param rango 7, 30 o 90 (tres meses aproximados).
     */
    @GetMapping("/activity")
    public ResponseEntity<DashboardActivityDTO> actividad(
            @RequestParam(name = "rango", defaultValue = "7") int rango) {
        int dias = switch (rango) {
            case 30 -> 30;
            case 90 -> 90;
            default -> 7;
        };
        return ResponseEntity.ok(dashboardService.obtenerActividad(dias));
    }

    private static String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
