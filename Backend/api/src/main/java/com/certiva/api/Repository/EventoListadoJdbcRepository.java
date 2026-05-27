package com.certiva.api.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.certiva.api.enums.EstadoOperativoEvento;
import com.certiva.api.enums.ModalidadEvento;
import com.certiva.api.enums.TipoEventoEnum;

/**
 * Listados de eventos vía SQL directo (evita hidratar herencia JOINED, causa habitual de HTTP 500).
 */
@Repository
public class EventoListadoJdbcRepository {

    private static final Logger log = LoggerFactory.getLogger(EventoListadoJdbcRepository.class);

    /** PostgreSQL: tablas en minúsculas (usuario, evento, inscripcion). */
    private static final String[][] TABLA_EVENTO_USUARIO = {
            { "evento", "usuario" },
    };

    private static final String[] TABLA_INSCRIPCION = { "inscripcion", "\"Inscripcion\"" };

    private final JdbcTemplate jdbc;

    public EventoListadoJdbcRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public record EventoListadoRow(
            Long idEvento,
            String nombreEvento,
            TipoEventoEnum tipoEvento,
            ModalidadEvento modalidad,
            Integer aforoMaximo,
            Boolean estado,
            EstadoOperativoEvento estadoOperativo,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            String creadorNombres,
            String creadorApellidos) {
    }

    public record CatalogoPublicoRow(
            Long idEvento,
            String nombreEvento,
            String descripcion,
            TipoEventoEnum tipoEvento,
            ModalidadEvento modalidad,
            Integer aforoMaximo,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            String ubicacion,
            String enlaceVirtual,
            Double precio,
            String rutaImagenPromocional,
            String creadorNombres,
            String creadorApellidos) {
    }

    private record CatalogoExtrasRow(
            Long idEvento,
            String descripcion,
            String ubicacion,
            String enlaceVirtual,
            Double costo,
            String imagenPromocional) {
    }

    private static final String[] SQL_CATALOGO_MAPA = {
            """
            SELECT e.id_evento, e.titulo, e.descripcion,
                   CAST(e.tipo_evento AS varchar) AS tipo_evento,
                   CAST(e.modalidad AS varchar) AS modalidad,
                   e.cupos, e.estado, e.fecha_inicio, e.fecha_fin,
                   e.ubicacion, e.enlace_virtual, e.costo, e.imagen_promocional
            FROM evento e
            ORDER BY e.fecha_inicio ASC
            """,
            """
            SELECT e.id_evento, e.titulo, NULL::text AS descripcion,
                   'CURSO' AS tipo_evento, 'PRESENCIAL' AS modalidad,
                   e.cupos, e.estado, e.fecha_inicio, e.fecha_fin,
                   NULL::varchar AS ubicacion, NULL::varchar AS enlace_virtual,
                   COALESCE(e.costo, 0) AS costo, NULL::varchar AS imagen_promocional
            FROM evento e
            ORDER BY e.fecha_inicio ASC
            """,
    };

    /**
     * Catálogo público sin cargar entidades JPA (evita 500 por herencia JOINED / TipoEvento nulo).
     */
    public List<CatalogoPublicoRow> listarCatalogoPublico() {
        LocalDateTime ahora = LocalDateTime.now();
        for (String sql : SQL_CATALOGO_MAPA) {
            try {
                List<Map<String, Object>> maps = jdbc.queryForList(sql);
                List<CatalogoPublicoRow> filas = maps.stream()
                        .filter(EventoListadoJdbcRepository::esActivoDesdeMapa)
                        .filter(m -> {
                            LocalDateTime fin = toLocalDateTime(valorMapa(m, "fecha_fin"));
                            return fin != null && fin.isAfter(ahora);
                        })
                        .map(EventoListadoJdbcRepository::catalogoDesdeMapa)
                        .filter(Objects::nonNull)
                        .sorted(Comparator.comparing(
                                CatalogoPublicoRow::fechaInicio,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                        .toList();
                if (!filas.isEmpty()) {
                    return enriquecerConCreador(filas);
                }
                if (!maps.isEmpty()) {
                    return List.of();
                }
            } catch (Exception ex) {
                log.warn("Catálogo por mapa falló: {}", ex.getMessage());
            }
        }
        return listarCatalogoPublicoLegacy(ahora);
    }

    private List<CatalogoPublicoRow> listarCatalogoPublicoLegacy(LocalDateTime ahora) {
        try {
            List<EventoListadoRow> filas = listarConFiltros(null, null, null, null, null, null);
            List<EventoListadoRow> vigentes = filas.stream()
                    .filter(EventoListadoJdbcRepository::esEventoActivo)
                    .filter(e -> e.fechaFin() != null && e.fechaFin().isAfter(ahora))
                    .toList();
            if (vigentes.isEmpty()) {
                return List.of();
            }
            Map<Long, CatalogoExtrasRow> extras = cargarExtrasCatalogo(
                    vigentes.stream().map(EventoListadoRow::idEvento).toList());
            return vigentes.stream()
                    .map(f -> combinarCatalogo(f, extras.get(f.idEvento())))
                    .toList();
        } catch (Exception ex) {
            log.error("Catálogo legacy JDBC falló", ex);
            return List.of();
        }
    }

    private List<CatalogoPublicoRow> enriquecerConCreador(List<CatalogoPublicoRow> filas) {
        List<Long> ids = filas.stream().map(CatalogoPublicoRow::idEvento).filter(Objects::nonNull).toList();
        if (ids.isEmpty()) {
            return filas;
        }
        Map<Long, String[]> creadores = cargarNombresCreador(ids);
        return filas.stream()
                .map(f -> {
                    String[] n = creadores.get(f.idEvento());
                    if (n == null) {
                        return f;
                    }
                    return new CatalogoPublicoRow(
                            f.idEvento(), f.nombreEvento(), f.descripcion(), f.tipoEvento(), f.modalidad(),
                            f.aforoMaximo(), f.fechaInicio(), f.fechaFin(), f.ubicacion(), f.enlaceVirtual(),
                            f.precio(), f.rutaImagenPromocional(), n[0], n[1]);
                })
                .toList();
    }

    private Map<Long, String[]> cargarNombresCreador(List<Long> ids) {
        String placeholders = String.join(",", ids.stream().map(id -> "?").toList());
        for (String[] tablas : TABLA_EVENTO_USUARIO) {
            String sql = """
                    SELECT e.id_evento, u.nombres, u.apellidos
                    FROM %s e
                    LEFT JOIN %s u ON u.id_usuario = e.id_creador
                    WHERE e.id_evento IN (%s)
                    """
                    .formatted(tablas[0], tablas[1], placeholders);
            try {
                Map<Long, String[]> mapa = new HashMap<>();
                jdbc.query(sql, rs -> {
                    mapa.put(rs.getLong("id_evento"), new String[] {
                            rs.getString("nombres"), rs.getString("apellidos")
                    });
                }, ids.toArray());
                return mapa;
            } catch (Exception ex) {
                log.debug("Nombres creador catálogo: {}", ex.getMessage());
            }
        }
        return Map.of();
    }

    private static CatalogoPublicoRow catalogoDesdeMapa(Map<String, Object> m) {
        Long id = toLong(valorMapa(m, "id_evento"));
        if (id == null) {
            return null;
        }
        return new CatalogoPublicoRow(
                id,
                toString(valorMapa(m, "titulo")),
                toString(valorMapa(m, "descripcion")),
                parseTipo(toString(valorMapa(m, "tipo_evento"))),
                parseModalidad(toString(valorMapa(m, "modalidad"))),
                toInteger(valorMapa(m, "cupos")),
                toLocalDateTime(valorMapa(m, "fecha_inicio")),
                toLocalDateTime(valorMapa(m, "fecha_fin")),
                toString(valorMapa(m, "ubicacion")),
                toString(valorMapa(m, "enlace_virtual")),
                toDouble(valorMapa(m, "costo")),
                toString(valorMapa(m, "imagen_promocional")),
                null,
                null);
    }

    private static boolean esActivoDesdeMapa(Map<String, Object> m) {
        Object st = valorMapa(m, "estado");
        if (st == null) {
            return true;
        }
        if (st instanceof Boolean b) {
            return b;
        }
        if (st instanceof Number n) {
            return n.intValue() != 0;
        }
        String s = st.toString().trim();
        return s.isEmpty() || "true".equalsIgnoreCase(s) || "t".equalsIgnoreCase(s) || "1".equals(s);
    }

    private static Object valorMapa(Map<String, Object> m, String col) {
        if (m.containsKey(col)) {
            return m.get(col);
        }
        String lower = col.toLowerCase();
        if (m.containsKey(lower)) {
            return m.get(lower);
        }
        return null;
    }

    private static Long toLong(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.parseLong(o.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static Integer toInteger(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(o.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static Double toDouble(Object o) {
        if (o == null) {
            return 0.0;
        }
        if (o instanceof Number n) {
            return n.doubleValue();
        }
        try {
            return Double.parseDouble(o.toString());
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    private static String toString(Object o) {
        return o != null ? o.toString() : null;
    }

    private static LocalDateTime toLocalDateTime(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof LocalDateTime ldt) {
            return ldt;
        }
        if (o instanceof Timestamp ts) {
            return ts.toLocalDateTime();
        }
        if (o instanceof java.util.Date d) {
            return new Timestamp(d.getTime()).toLocalDateTime();
        }
        return null;
    }

    private Map<Long, CatalogoExtrasRow> cargarExtrasCatalogo(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        String placeholders = String.join(",", ids.stream().map(id -> "?").toList());
        String sql = """
                SELECT id_evento, descripcion, ubicacion, enlace_virtual, costo, imagen_promocional
                FROM evento
                WHERE id_evento IN (%s)
                """.formatted(placeholders);
        try {
            Map<Long, CatalogoExtrasRow> mapa = new HashMap<>();
            jdbc.query(sql, rs -> {
                mapa.put(
                        rs.getLong("id_evento"),
                        new CatalogoExtrasRow(
                                rs.getLong("id_evento"),
                                rs.getString("descripcion"),
                                rs.getString("ubicacion"),
                                rs.getString("enlace_virtual"),
                                rs.getObject("costo") != null ? rs.getDouble("costo") : 0.0,
                                rs.getString("imagen_promocional")));
            }, ids.toArray());
            return mapa;
        } catch (Exception ex) {
            log.debug("Extras catálogo no disponibles: {}", ex.getMessage());
            return Map.of();
        }
    }

    private static CatalogoPublicoRow combinarCatalogo(EventoListadoRow f, CatalogoExtrasRow ex) {
        if (ex == null) {
            return new CatalogoPublicoRow(
                    f.idEvento(),
                    f.nombreEvento(),
                    null,
                    f.tipoEvento(),
                    f.modalidad(),
                    f.aforoMaximo(),
                    f.fechaInicio(),
                    f.fechaFin(),
                    null,
                    null,
                    0.0,
                    null,
                    f.creadorNombres(),
                    f.creadorApellidos());
        }
        return new CatalogoPublicoRow(
                f.idEvento(),
                f.nombreEvento(),
                ex.descripcion(),
                f.tipoEvento(),
                f.modalidad(),
                f.aforoMaximo(),
                f.fechaInicio(),
                f.fechaFin(),
                ex.ubicacion(),
                ex.enlaceVirtual(),
                ex.costo() != null ? ex.costo() : 0.0,
                ex.imagenPromocional(),
                f.creadorNombres(),
                f.creadorApellidos());
    }

    public List<EventoListadoRow> listarConFiltros(
            Boolean soloActivos,
            ModalidadEvento modalidad,
            TipoEventoEnum tipo,
            LocalDateTime desde,
            LocalDateTime hasta,
            EstadoOperativoEvento estadoOperativo) {

        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        List<Object> params = new ArrayList<>();

        if (Boolean.TRUE.equals(soloActivos)) {
            where.append(" AND COALESCE(CAST(e.estado AS varchar), 'true') IN ('true', 't', '1', 'TRUE')");
        } else if (Boolean.FALSE.equals(soloActivos)) {
            where.append(" AND COALESCE(CAST(e.estado AS varchar), 'true') IN ('false', 'f', '0', 'FALSE')");
        }
        if (modalidad != null) {
            where.append(" AND UPPER(TRIM(CAST(e.modalidad AS varchar))) = ?");
            params.add(modalidad.name());
        }
        if (tipo != null) {
            where.append(" AND UPPER(TRIM(CAST(e.tipo_evento AS varchar))) = ?");
            params.add(tipo.name());
        }
        if (desde != null) {
            where.append(" AND e.fecha_inicio >= ?");
            params.add(Timestamp.valueOf(desde));
        }
        if (hasta != null) {
            where.append(" AND e.fecha_inicio <= ?");
            params.add(Timestamp.valueOf(hasta));
        }
        if (estadoOperativo != null) {
            where.append(" AND UPPER(TRIM(CAST(e.estado_operativo AS varchar))) = ?");
            params.add(estadoOperativo.name());
        }

        Exception ultima = null;
        for (String[] tablas : TABLA_EVENTO_USUARIO) {
            String eventoTbl = tablas[0];
            String usuarioTbl = tablas[1];
            String sql = """
                    SELECT e.id_evento,
                           e.titulo,
                           COALESCE(UPPER(TRIM(CAST(e.tipo_evento AS varchar))), 'CURSO') AS tipo_evento,
                           CAST(e.modalidad AS varchar) AS modalidad,
                           e.cupos,
                           e.estado,
                           CAST(e.estado_operativo AS varchar) AS estado_operativo,
                           e.fecha_inicio,
                           e.fecha_fin,
                           u.nombres AS creador_nombres,
                           u.apellidos AS creador_apellidos
                    FROM %s e
                    LEFT JOIN %s u ON u.id_usuario = e.id_creador
                    """
                    .formatted(eventoTbl, usuarioTbl)
                    + where
                    + " ORDER BY e.fecha_inicio DESC";

            try {
                List<EventoListadoRow> filas = jdbc.query(sql, rowMapper(), params.toArray());
                log.debug("Listado eventos OK con tablas {} / {}", eventoTbl, usuarioTbl);
                return filas;
            } catch (Exception ex) {
                ultima = ex;
                log.debug("Listado eventos falló con {} / {}: {}", eventoTbl, usuarioTbl, ex.getMessage());
            }
        }

        try {
            String sqlSinJoin = """
                    SELECT e.id_evento,
                           e.titulo,
                           COALESCE(UPPER(TRIM(CAST(e.tipo_evento AS varchar))), 'CURSO') AS tipo_evento,
                           CAST(e.modalidad AS varchar) AS modalidad,
                           e.cupos,
                           e.estado,
                           CAST(e.estado_operativo AS varchar) AS estado_operativo,
                           e.fecha_inicio,
                           e.fecha_fin,
                           NULL::varchar AS creador_nombres,
                           NULL::varchar AS creador_apellidos
                    FROM evento e
                    """
                    + where
                    + " ORDER BY e.fecha_inicio DESC";
            log.warn("Listado eventos sin JOIN a usuario (revisar FK id_creador)");
            return jdbc.query(sqlSinJoin, rowMapper(), params.toArray());
        } catch (Exception ex) {
            ultima = ex;
        }

        try {
            String sqlMinimo = """
                    SELECT e.id_evento,
                           e.titulo,
                           'CURSO' AS tipo_evento,
                           'PRESENCIAL' AS modalidad,
                           e.cupos,
                           e.estado,
                           'PROXIMO' AS estado_operativo,
                           e.fecha_inicio,
                           e.fecha_fin,
                           NULL::varchar AS creador_nombres,
                           NULL::varchar AS creador_apellidos
                    FROM evento e
                    """
                    + where
                    + " ORDER BY e.fecha_inicio DESC";
            log.warn("Listado eventos con SQL mínimo (sin tipo_evento/modalidad en SELECT)");
            return jdbc.query(sqlMinimo, rowMapper(), params.toArray());
        } catch (Exception ex) {
            ultima = ex;
        }

        log.error("No se pudo listar eventos JDBC", ultima);
        throw new IllegalStateException("No se pudo listar eventos (revise tablas evento/usuario)", ultima);
    }

    private static boolean esEventoActivo(EventoListadoRow e) {
        return e.estado() == null || Boolean.TRUE.equals(e.estado());
    }

    public Map<Long, Long> contarInscritosActivosPorEvento(Collection<Long> idsEvento) {
        if (idsEvento == null || idsEvento.isEmpty()) {
            return Map.of();
        }
        String placeholders = String.join(",", idsEvento.stream().map(id -> "?").toList());
        String sqlBase = """
                SELECT i.id_evento, COUNT(*) AS total
                FROM %s i
                WHERE i.id_evento IN (%s)
                  AND UPPER(TRIM(i.estado)) NOT IN ('INACTIVO', 'CANCELLED')
                GROUP BY i.id_evento
                """;

        for (String tabla : TABLA_INSCRIPCION) {
            try {
                String sql = sqlBase.formatted(tabla, placeholders);
                Map<Long, Long> mapa = new HashMap<>();
                List<Object> params = new ArrayList<>(idsEvento);
                jdbc.query(sql, rs -> {
                    mapa.put(rs.getLong("id_evento"), rs.getLong("total"));
                }, params.toArray());
                return mapa;
            } catch (Exception ex) {
                log.debug("Conteo inscripciones falló con tabla {}: {}", tabla, ex.getMessage());
            }
        }
        return Map.of();
    }

    private static RowMapper<EventoListadoRow> rowMapper() {
        return (ResultSet rs, int rowNum) -> {
            TipoEventoEnum tipoEnum = parseTipo(rs.getString("tipo_evento"));
            ModalidadEvento mod = parseModalidad(rs.getString("modalidad"));
            return new EventoListadoRow(
                    rs.getLong("id_evento"),
                    rs.getString("titulo"),
                    tipoEnum,
                    mod,
                    (Integer) rs.getObject("cupos"),
                    leerEstado(rs),
                    parseEstadoOperativo(rs.getString("estado_operativo")),
                    toLocalDateTime(rs.getTimestamp("fecha_inicio")),
                    toLocalDateTime(rs.getTimestamp("fecha_fin")),
                    rs.getString("creador_nombres"),
                    rs.getString("creador_apellidos"));
        };
    }

    private static EstadoOperativoEvento parseEstadoOperativo(String raw) {
        if (raw == null || raw.isBlank()) {
            return EstadoOperativoEvento.PROXIMO;
        }
        try {
            return EstadoOperativoEvento.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return EstadoOperativoEvento.PROXIMO;
        }
    }

    private static LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts != null ? ts.toLocalDateTime() : null;
    }

    private static Boolean leerEstado(ResultSet rs) throws SQLException {
        Object val = rs.getObject("estado");
        if (val == null) {
            return Boolean.TRUE;
        }
        if (val instanceof Boolean b) {
            return b;
        }
        if (val instanceof Number n) {
            return n.intValue() != 0;
        }
        String s = val.toString().trim();
        if (s.isEmpty()) {
            return Boolean.TRUE;
        }
        return "true".equalsIgnoreCase(s) || "t".equalsIgnoreCase(s) || "1".equals(s);
    }

    private static TipoEventoEnum parseTipo(String raw) {
        if (raw == null || raw.isBlank()) {
            return TipoEventoEnum.CURSO;
        }
        try {
            return TipoEventoEnum.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return TipoEventoEnum.CURSO;
        }
    }

    private static ModalidadEvento parseModalidad(String raw) {
        if (raw == null || raw.isBlank()) {
            return ModalidadEvento.PRESENCIAL;
        }
        try {
            return ModalidadEvento.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return ModalidadEvento.PRESENCIAL;
        }
    }
}
