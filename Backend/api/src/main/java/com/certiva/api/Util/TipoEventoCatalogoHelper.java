package com.certiva.api.Util;

import java.util.List;

import org.springframework.stereotype.Component;

import com.certiva.api.Entity.TipoEvento;
import com.certiva.api.Repository.TipoEventoRepository;
import com.certiva.api.enums.TipoEventoEnum;

/**
 * Resuelve o crea filas en la tabla legada {@code TipoEvento} para {@code id_tipo_evento}.
 */
@Component
public class TipoEventoCatalogoHelper {

    private final TipoEventoRepository tipoEventoRepository;

    public TipoEventoCatalogoHelper(TipoEventoRepository tipoEventoRepository) {
        this.tipoEventoRepository = tipoEventoRepository;
    }

    public TipoEvento resolverOCrear(TipoEventoEnum tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de evento es obligatorio.");
        }
        for (String candidato : nombresCandidatosCatalogo(tipo)) {
            var encontrado = tipoEventoRepository.findFirstByNombreIgnoreCase(candidato);
            if (encontrado.isPresent()) {
                return encontrado.get();
            }
        }
        String clave = normalizarNombreTipo(tipo.name());
        var porCoincidencia = tipoEventoRepository.findAll().stream()
                .filter(t -> {
                    String n = normalizarNombreTipo(t.getNombre());
                    return n.equals(clave) || n.startsWith(clave) || n.contains(clave);
                })
                .findFirst();
        if (porCoincidencia.isPresent()) {
            return porCoincidencia.get();
        }
        return crearCatalogoTipoEvento(tipo);
    }

    public void asegurarCatalogoBase() {
        for (TipoEventoEnum tipo : TipoEventoEnum.values()) {
            resolverOCrear(tipo);
        }
    }

    /**
     * Garantiza la FK {@code id_tipo_evento} antes de persistir (evita 409 NOT NULL en PostgreSQL).
     */
    public void asignarCatalogoSiFalta(com.certiva.api.Entity.Evento evento) {
        if (evento == null) {
            throw new IllegalArgumentException("El evento no puede ser nulo.");
        }
        if (evento.getCatalogoTipoEvento() != null) {
            return;
        }
        if (evento.getTipoEvento() == null) {
            throw new IllegalArgumentException("El tipo de evento (enum) es obligatorio.");
        }
        evento.setCatalogoTipoEvento(resolverOCrear(evento.getTipoEvento()));
    }

    private TipoEvento crearCatalogoTipoEvento(TipoEventoEnum tipo) {
        TipoEvento nuevo = new TipoEvento();
        nuevo.setNombre(etiquetaCatalogoTipo(tipo));
        nuevo.setDescripcion("Tipo de evento " + tipo.name());
        nuevo.setTieneEvaluacion(tipo == TipoEventoEnum.CURSO || tipo == TipoEventoEnum.TALLER);
        nuevo.setTieneGanador(tipo == TipoEventoEnum.HACKATHON || tipo == TipoEventoEnum.FERIA);
        return tipoEventoRepository.save(nuevo);
    }

    private static List<String> nombresCandidatosCatalogo(TipoEventoEnum tipo) {
        return switch (tipo) {
            case CURSO -> List.of("CURSO", "Curso", "Curso académico", "Curso Academico");
            case HACKATHON -> List.of("HACKATHON", "Hackathon");
            case TALLER -> List.of("TALLER", "Taller");
            case FERIA -> List.of("FERIA", "Feria", "Feria de proyectos", "Feria de Proyectos");
        };
    }

    private static String etiquetaCatalogoTipo(TipoEventoEnum tipo) {
        return switch (tipo) {
            case CURSO -> "Curso";
            case HACKATHON -> "Hackathon";
            case TALLER -> "Taller";
            case FERIA -> "Feria";
        };
    }

    private static String normalizarNombreTipo(String nombre) {
        if (nombre == null) {
            return "";
        }
        return nombre.toLowerCase().replaceAll("[^a-z0-9]", "");
    }
}
