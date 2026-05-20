package com.certiva.api.Util;

import java.io.ByteArrayOutputStream;

import org.springframework.stereotype.Component;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;

@Component
public class PdfCertificadoGenerator {

    private final QrCodeGenerator qrCodeGenerator;

    public PdfCertificadoGenerator(QrCodeGenerator qrCodeGenerator) {
        this.qrCodeGenerator = qrCodeGenerator;
    }

    public byte[] generarPdf(String tituloEvento, String nombreParticipante, String codigoVerificacion,
                             String urlVerificacionPublica) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf);
            doc.add(new Paragraph("Certificado de asistencia").setBold().setFontSize(18));
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Evento: " + nullSafe(tituloEvento)));
            doc.add(new Paragraph("Participante: " + nullSafe(nombreParticipante)));
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Código de verificación: " + nullSafe(codigoVerificacion)).setFontSize(12));
            if (urlVerificacionPublica != null && !urlVerificacionPublica.isBlank()) {
                doc.add(new Paragraph(" "));
                doc.add(new Paragraph("Escanee el código QR para validar la autenticidad:").setFontSize(10));
                byte[] qrPng = qrCodeGenerator.generarPng(urlVerificacionPublica, 160);
                Image qr = new Image(ImageDataFactory.create(qrPng)).scaleToFit(140, 140);
                doc.add(qr);
                doc.add(new Paragraph(urlVerificacionPublica).setFontSize(8));
            }
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Emitido electrónicamente por Certiva.").setFontSize(9));
            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el PDF del certificado", e);
        }
    }

    private static String nullSafe(String s) {
        return s == null ? "" : s;
    }
}
