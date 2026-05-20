package com.certiva.api.Impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.certiva.api.DTO.DashboardActivityDTO;
import com.certiva.api.DTO.DashboardActivityPointDTO;
import com.certiva.api.DTO.DashboardDTO;
import com.certiva.api.Entity.Usuario;
import com.certiva.api.Repository.CertificadoRepository;
import com.certiva.api.Repository.EventoRepository;
import com.certiva.api.Repository.InscripcionRepository;
import com.certiva.api.Repository.UsuarioRepository;
import com.certiva.api.Service.AuditoriaService;
import com.certiva.api.Service.DashboardService;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final DateTimeFormatter AUDIT_FECHA_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ROOT);

    private final UsuarioRepository usuarioRepository;
    private final EventoRepository eventoRepository;
    private final InscripcionRepository inscripcionRepository;
    private final CertificadoRepository certificadoRepository;
    private final AuditoriaService auditoriaService;

    public DashboardServiceImpl(UsuarioRepository usuarioRepository,
                                EventoRepository eventoRepository,
                                InscripcionRepository inscripcionRepository,
                                CertificadoRepository certificadoRepository,
                                AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.eventoRepository = eventoRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.certificadoRepository = certificadoRepository;
        this.auditoriaService = auditoriaService;
    }

    @Override
    @Transactional
    public DashboardDTO obtenerEstadisticas(String ipRemota) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth != null ? auth.getName() : null;
        Usuario admin = correo != null ? usuarioRepository.findByCorreo(correo).orElse(null) : null;

        String ahoraTxt = LocalDateTime.now().format(AUDIT_FECHA_FMT);
        auditoriaService.registrarAuditoria(
                "CONSULTA_DASHBOARD_STATS",
                "Acceso a estadísticas del panel (GET /api/dashboard/stats). Fecha: " + ahoraTxt + ". IP: " + ipRemota,
                ipRemota,
                admin);

        Map<String, Long> distribucionRoles = construirDistribucionRoles(usuarioRepository.countUsuariosPorNombreRol());

        LocalDateTime ahora = LocalDateTime.now();

        return DashboardDTO.builder()
                .totalUsuarios(usuarioRepository.count())
                .eventosActivos(eventoRepository.countEventosActivosEn(ahora))
                .asistenciasTotales(inscripcionRepository.countAsistenciasPorEstado())
                .certificadosEmitidos(certificadoRepository.count())
                .distribucionRoles(distribucionRoles)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardActivityDTO obtenerActividad(int dias) {
        int ventana = dias <= 0 ? 7 : Math.min(dias, 366);
        LocalDate fin = LocalDate.now();
        LocalDate inicio = fin.minusDays(ventana - 1L);
        LocalDateTime desde = inicio.atStartOfDay();
        LocalDateTime hastaExclusivo = fin.plusDays(1).atStartOfDay();

        Map<LocalDate, Long> asistenciasPorDia = mapaConteosPorDia(
                inscripcionRepository.countAsistenciasPorDia(desde, hastaExclusivo));
        Map<LocalDate, Long> certificadosPorDia = mapaConteosPorDia(
                certificadoRepository.countCertificadosPorDia(desde, hastaExclusivo));

        List<DashboardActivityPointDTO> puntos = new java.util.ArrayList<>();
        for (LocalDate d = inicio; !d.isAfter(fin); d = d.plusDays(1)) {
            puntos.add(DashboardActivityPointDTO.builder()
                    .fecha(d)
                    .asistencias(asistenciasPorDia.getOrDefault(d, 0L))
                    .certificados(certificadosPorDia.getOrDefault(d, 0L))
                    .build());
        }

        return DashboardActivityDTO.builder()
                .rangoDias(ventana)
                .puntos(puntos)
                .auditoriaReciente(auditoriaService.listarUltimasAuditorias(80))
                .build();
    }

    /**
     * Claves en español para Chart.js, siempre las cuatro categorías (0 si no hay usuarios con ese rol en BD).
     * Mapeo desde nombres {@code ROLE_*} en tabla Rol.
     */
    private static Map<String, Long> construirDistribucionRoles(List<Object[]> filas) {
        Map<String, Long> porNombreRol = new HashMap<>();
        for (Object[] fila : filas) {
            String nombreRol = fila[0] != null ? ((String) fila[0]).trim() : "";
            long cantidad = fila[1] instanceof Number n ? n.longValue() : 0L;
            if (!nombreRol.isEmpty()) {
                porNombreRol.merge(nombreRol, cantidad, Long::sum);
            }
        }

        long estudiantes = porNombreRol.getOrDefault("ROLE_ESTUDIANTE", 0L);
        long profesores = porNombreRol.getOrDefault("ROLE_PROFESOR", 0L);
        long monitores = porNombreRol.getOrDefault("ROLE_MONITOR", 0L);
        long administradores = porNombreRol.getOrDefault("ROLE_ADMIN", 0L);

        Map<String, Long> ordenado = new LinkedHashMap<>();
        ordenado.put("Estudiantes", estudiantes);
        ordenado.put("Profesores", profesores);
        ordenado.put("Monitores", monitores);
        ordenado.put("Administradores", administradores);
        return ordenado;
    }

    private static Map<LocalDate, Long> mapaConteosPorDia(List<Object[]> filas) {
        Map<LocalDate, Long> mapa = new LinkedHashMap<>();
        for (Object[] fila : filas) {
            LocalDate dia = aLocalDate(fila[0]);
            long cnt = fila[1] instanceof Number n ? n.longValue() : 0L;
            if (dia != null) {
                mapa.put(dia, cnt);
            }
        }
        return mapa;
    }

    private static LocalDate aLocalDate(Object valor) {
        if (valor == null) {
            return null;
        }
        if (valor instanceof LocalDate ld) {
            return ld;
        }
        if (valor instanceof java.sql.Date sd) {
            return sd.toLocalDate();
        }
        if (valor instanceof java.util.Date ud) {
            return new java.sql.Date(ud.getTime()).toLocalDate();
        }
        return null;
    }
}
