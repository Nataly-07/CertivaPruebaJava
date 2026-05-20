package com.certiva.api.Controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.certiva.api.Util.QrCodeGenerator;

@RestController
@RequestMapping("/api/public/qr")
public class QrPublicController {

    private final QrCodeGenerator qrCodeGenerator;

    public QrPublicController(QrCodeGenerator qrCodeGenerator) {
        this.qrCodeGenerator = qrCodeGenerator;
    }

    @GetMapping(produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generarQr(@RequestParam("data") String data,
                                            @RequestParam(value = "size", defaultValue = "256") int size) {
        byte[] png = qrCodeGenerator.generarPng(data, Math.min(Math.max(size, 64), 512));
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
                .body(png);
    }
}
