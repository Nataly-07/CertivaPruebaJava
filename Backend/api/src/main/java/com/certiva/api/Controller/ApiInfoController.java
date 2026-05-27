package com.certiva.api.Controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiInfoController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> raiz() {
        return ResponseEntity.ok(Map.of(
                "nombre", "Certiva API",
                "version", "1.0",
                "mensaje", "Backend activo. Este servicio expone JSON en /api/ — abra la app Angular en http://localhost:4200",
                "swagger", "/swagger-ui/index.html",
                "login", "/api/usuarios/login"));
    }
}
