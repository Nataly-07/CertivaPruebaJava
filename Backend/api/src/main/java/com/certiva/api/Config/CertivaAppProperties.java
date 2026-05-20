package com.certiva.api.Config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "certiva")
public class CertivaAppProperties {

    private Frontend frontend = new Frontend();

    @Data
    public static class Frontend {
        private String baseUrl = "http://localhost:4200";
    }

    public String urlInscripcionPorCodigoDifusion(String codigoDifusion) {
        String base = frontend.getBaseUrl().replaceAll("/$", "");
        return base + "/portal/inscribir/d/" + codigoDifusion;
    }

    public String urlVerificarCertificado(String codigoValidacion) {
        String base = frontend.getBaseUrl().replaceAll("/$", "");
        return base + "/verificar-certificado?codigo=" + codigoValidacion;
    }
}
