package com.certiva.api.Service;

import org.springframework.stereotype.Service;

import com.certiva.api.Entity.CursoEvento;
import com.certiva.api.Entity.Evento;
import com.certiva.api.Entity.Inscripcion;
import com.certiva.api.Entity.ResultadoEvaluacion;
import com.certiva.api.Exception.OperacionNoPermitidaException;
import com.certiva.api.Repository.ResultadoEvaluacionRepository;
import com.certiva.api.Util.InscripcionEstadoHelper;
import com.certiva.api.enums.TipoEventoEnum;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CertificadoElegibilidadService {

    private final ResultadoEvaluacionRepository resultadoEvaluacionRepository;

    /**
     * Evalúa si la inscripción cumple las reglas del evento para emitir certificado automático.
     */
    public void validarElegibilidad(Inscripcion inscripcion) {
        Evento evento = inscripcion.getEvento();
        if (evento == null) {
            throw new OperacionNoPermitidaException("Evento no asociado a la inscripción.");
        }
        if (!InscripcionEstadoHelper.tieneAsistenciaConfirmada(inscripcion.getEstado())) {
            throw new OperacionNoPermitidaException("La asistencia debe estar confirmada para certificar.");
        }

        if (evento.getTipoEvento() == TipoEventoEnum.CURSO && evento instanceof CursoEvento curso) {
            validarCurso(inscripcion, curso);
            return;
        }

        // Hackathon, feria, taller y otros: asistencia confirmada es suficiente.
    }

    public boolean puedeEmitirCertificado(Inscripcion inscripcion) {
        try {
            validarElegibilidad(inscripcion);
            return true;
        } catch (OperacionNoPermitidaException ex) {
            return false;
        }
    }

    public String motivoPendienteCertificado(Inscripcion inscripcion) {
        try {
            validarElegibilidad(inscripcion);
            return null;
        } catch (OperacionNoPermitidaException ex) {
            return ex.getMessage();
        }
    }

    private void validarCurso(Inscripcion inscripcion, CursoEvento curso) {
        ResultadoEvaluacion resultado = resultadoEvaluacionRepository
                .findFirstByInscripcion_IdInscripcionOrderByIdDesc(inscripcion.getIdInscripcion())
                .orElseThrow(() -> new OperacionNoPermitidaException(
                        "Certificado pendiente: debe registrarse la nota del estudiante."));

        Double nota = resultado.getNota();
        if (nota == null) {
            throw new OperacionNoPermitidaException("Certificado pendiente: nota no registrada.");
        }

        Double minima = curso.getNotaMinimaAprobacion();
        if (minima != null && nota < minima) {
            throw new OperacionNoPermitidaException(
                    "Certificado no emitido: la nota " + nota + " no alcanza el mínimo " + minima + ".");
        }

        Integer pctMin = curso.getPorcentajeAsistenciaMinimo();
        if (pctMin != null && pctMin > 0 && pctMin <= 100) {
            // Un check-in QR confirmado equivale al cumplimiento de asistencia en eventos de sesión única.
            return;
        }
    }

    private static String norm(String s) {
        return s == null ? "" : s.trim();
    }
}
