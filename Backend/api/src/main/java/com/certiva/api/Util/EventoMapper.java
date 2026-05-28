package com.certiva.api.Util;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.certiva.api.Config.CertivaAppProperties;
import com.certiva.api.DTO.DetalleCursoDTO;
import com.certiva.api.DTO.DetalleFeriaDTO;
import com.certiva.api.DTO.DetalleHackathonDTO;
import com.certiva.api.DTO.DetalleTallerDTO;
import com.certiva.api.DTO.EventoDTO;
import com.certiva.api.DTO.EventoFilaAdminDTO;
import com.certiva.api.DTO.UsuarioStaffDTO;
import com.certiva.api.Entity.CursoEvento;
import com.certiva.api.Entity.Evento;
import com.certiva.api.Entity.FeriaProyectoEvento;
import com.certiva.api.Entity.HackathonEvento;
import com.certiva.api.Entity.TallerEvento;
import com.certiva.api.Entity.Usuario;
import com.certiva.api.enums.TipoEventoEnum;

@Component
public class EventoMapper {

    private final CertivaAppProperties appProperties;

    public EventoMapper(CertivaAppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public EventoDTO toDto(Evento e, boolean incluirStaff) {
        EventoDTO dto = new EventoDTO();
        dto.setIdEvento(e.getIdEvento());
        dto.setNombreEvento(e.getNombreEvento());
        dto.setDescripcion(e.getDescripcion());
        dto.setTipoEvento(e.getTipoEvento());
        dto.setModalidad(e.getModalidad());
        dto.setFechaInicio(e.getFechaInicio());
        dto.setFechaFin(e.getFechaFin());
        dto.setUbicacion(e.getUbicacion());
        dto.setEnlaceVirtual(e.getEnlaceVirtual());
        dto.setAforoMaximo(e.getAforoMaximo());
        dto.setIntensidadHoraria(e.getIntensidadHoraria());
        dto.setPorcentajeAsistenciaMinimo(
                com.certiva.api.Util.EventoAsistenciaHelper.resolverPorcentajeMinimo(e));
        dto.setPrecio(e.getCosto());
        dto.setGratuito(e.isGratuito());
        dto.setCodigoDifusion(e.getCodigoDifusion());
        if (e.getCodigoDifusion() != null && !e.getCodigoDifusion().isBlank()) {
            dto.setUrlInscripcionPublica(appProperties.urlInscripcionPorCodigoDifusion(e.getCodigoDifusion()));
        }
        dto.setRutaImagenPromocional(e.getRutaImagenPromocional());
        dto.setRutaPensum(e.getRutaPensum());
        dto.setTextoDiploma(e.getTextoDiploma());
        dto.setFirmaDigitalProfesor(e.getFirmaDigitalProfesor());
        dto.setImagenPromocionalUrl(e.getRutaImagenPromocional());
        dto.setEstado(e.getEstado());
        dto.setEstadoOperativo(
                com.certiva.api.Util.EstadoOperativoEventoHelper.resolverOperativoVisible(
                        e, java.time.LocalDateTime.now()));
        dto.setIdUsuarioCreador(e.getUsuarioCreador() != null ? e.getUsuarioCreador().getIdUsuario() : null);
        dto.setIdProfesorLider(e.getUsuarioCreador() != null ? e.getUsuarioCreador().getIdUsuario() : null);
        dto.setProfesorLider(toStaff(e.getUsuarioCreador()));

        if (incluirStaff) {
            dto.setProfesoresColaboradores(staffList(e.getProfesoresColaboradores()));
            dto.setMonitoresAsignados(staffList(e.getMonitoresAsignados()));
        }

        mapearDetalleTipo(e, dto);
        return dto;
    }

    private void mapearDetalleTipo(Evento e, EventoDTO dto) {
        TipoEventoEnum tipo = e.getTipoEvento();
        if (tipo == null) {
            return;
        }
        switch (tipo) {
            case CURSO -> {
                if (e instanceof CursoEvento c) {
                    DetalleCursoDTO d = new DetalleCursoDTO();
                    d.setNivelAcademico(c.getNivelAcademico());
                    d.setNotaMinimaAprobacion(c.getNotaMinimaAprobacion());
                    d.setPorcentajeAsistenciaMinimo(
                            com.certiva.api.Util.EventoAsistenciaHelper.resolverPorcentajeMinimo(c));
                    dto.setDetalleCurso(d);
                }
            }
            case HACKATHON -> {
                if (e instanceof HackathonEvento h) {
                    DetalleHackathonDTO d = new DetalleHackathonDTO();
                    d.setRetoTecnicoCentral(h.getRetoTecnicoCentral());
                    d.setMinIntegrantes(h.getMinIntegrantes());
                    d.setMaxIntegrantes(h.getMaxIntegrantes());
                    d.setPremiosIncentivos(h.getPremiosIncentivos());
                    dto.setDetalleHackathon(d);
                }
            }
            case FERIA -> {
                if (e instanceof FeriaProyectoEvento f) {
                    DetalleFeriaDTO d = new DetalleFeriaDTO();
                    d.setCategoriaExhibicion(f.getCategoriaExhibicion());
                    d.setStackTecnologico(f.getStackTecnologico());
                    d.setCriteriosEvaluacion(f.getCriteriosEvaluacion());
                    dto.setDetalleFeria(d);
                }
            }
            case TALLER -> {
                if (e instanceof TallerEvento t) {
                    DetalleTallerDTO d = new DetalleTallerDTO();
                    d.setMaterialGuia(t.getMaterialGuia());
                    dto.setDetalleTaller(d);
                }
            }
            default -> { }
        }
    }

    private List<UsuarioStaffDTO> staffList(java.util.Set<Usuario> usuarios) {
        if (usuarios == null) {
            return List.of();
        }
        return usuarios.stream().map(this::toStaff).collect(Collectors.toList());
    }

    public EventoFilaAdminDTO toFilaAdmin(Evento e, long inscritosActivos) {
        EventoFilaAdminDTO dto = new EventoFilaAdminDTO();
        dto.setIdEvento(e.getIdEvento());
        dto.setNombreEvento(e.getNombreEvento());
        dto.setTipoEvento(e.getTipoEvento());
        dto.setModalidad(e.getModalidad());
        dto.setInstructorPrincipal(resolverInstructorPrincipal(e));
        dto.setInscritosActivos(inscritosActivos);
        dto.setAforoMaximo(e.getAforoMaximo());
        dto.setEstado(e.getEstado());
        dto.setFechaInicio(e.getFechaInicio());
        dto.setFechaFin(e.getFechaFin());
        return dto;
    }

    private String resolverInstructorPrincipal(Evento e) {
        if (e.getUsuarioCreador() != null) {
            return nombreCompleto(e.getUsuarioCreador());
        }
        return "Sin asignar";
    }

    private static String nombreCompleto(Usuario u) {
        String nombres = u.getNombres() != null ? u.getNombres().trim() : "";
        String apellidos = u.getApellidos() != null ? u.getApellidos().trim() : "";
        String completo = (nombres + " " + apellidos).trim();
        return completo.isEmpty() ? "Sin asignar" : completo;
    }

    public UsuarioStaffDTO toStaff(Usuario u) {
        if (u == null) {
            return null;
        }
        String codigo = "";
        if (u.getRoles() != null) {
            codigo = u.getRoles().stream()
                    .findFirst()
                    .map(r -> r.getNombre() != null ? r.getNombre().replace("ROLE_", "") : "")
                    .orElse("");
        }
        return UsuarioStaffDTO.builder()
                .idUsuario(u.getIdUsuario())
                .nombres(u.getNombres())
                .apellidos(u.getApellidos())
                .correo(u.getCorreo())
                .numeroDocumento(u.getNumeroDocumento())
                .codigoRol(codigo)
                .build();
    }
}
