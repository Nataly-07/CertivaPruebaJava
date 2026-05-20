package com.certiva.api.Controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
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
import org.springframework.web.bind.annotation.RestController;

import com.certiva.api.DTO.CertificadoVerificacionDTO;
import com.certiva.api.DTO.CrearCertificadoDTO;
import com.certiva.api.DTO.CertificadoDTO;
import com.certiva.api.DTO.CertificadoPortalDTO;
import com.certiva.api.Service.CertificadoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/certificados")
public class CertificadoController {

    private final CertificadoService _certificadoService;

    public CertificadoController(CertificadoService certificadoService) {
        this._certificadoService = certificadoService;
    }

    @GetMapping("/verificar/{codigo}")
    public ResponseEntity<CertificadoVerificacionDTO> verificarPublico(@PathVariable String codigo) {
        return ResponseEntity.ok(_certificadoService.verificarPublicoPorCodigo(codigo));
    }

    @PostMapping
    public ResponseEntity<CrearCertificadoDTO> crearCertificado(@Valid @RequestBody CrearCertificadoDTO certificadoDTO) {
        CrearCertificadoDTO nuevo = _certificadoService.crearCertificado(certificadoDTO);
        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }

    @GetMapping("/mis")
    public ResponseEntity<List<CertificadoPortalDTO>> listarMisCertificados() {
        return ResponseEntity.ok(_certificadoService.listarMisCertificados());
    }

    @GetMapping("/mis/{id}/pdf")
    public ResponseEntity<byte[]> descargarMiCertificadoPdf(@PathVariable Long id) {
        byte[] pdf = _certificadoService.descargarPdfMiCertificado(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"certificado-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PostMapping("/mis/inscripcion/{idInscripcion}/emitir")
    public ResponseEntity<CertificadoDTO> emitirMiCertificado(@PathVariable Long idInscripcion) {
        return ResponseEntity.ok(_certificadoService.emitirCertificadoMiInscripcion(idInscripcion));
    }

    @GetMapping
    public ResponseEntity<List<CertificadoDTO>> listarCertificados() {
        List<CertificadoDTO> listado = _certificadoService.listarCertificados();
        return ResponseEntity.ok(listado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CertificadoDTO> actualizarCertificado(@PathVariable Long id, @RequestBody CertificadoDTO certificadoDTO) {
        certificadoDTO.setIdCertificado(id);
        CertificadoDTO actualizado = _certificadoService.actualizarCertificado(certificadoDTO);
        return ResponseEntity.ok(actualizado);
    }

    @PutMapping("/{id}/inactivar")
    public ResponseEntity<String> inactivarCertificado(@PathVariable Long id) {
        String mensaje = _certificadoService.inactivarCertificado(id);
        return ResponseEntity.ok(mensaje);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> borrarCertificado(@PathVariable Long id) {
        boolean eliminado = _certificadoService.borrarCertificado(id);
        return ResponseEntity.ok(eliminado);
    }
}
