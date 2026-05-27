package com.certiva.api.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.certiva.api.Entity.Inscripcion;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {

    Optional<Inscripcion> findByTokenQr(String tokenQr);

    List<Inscripcion> findByUsuario_IdUsuarioOrderByFechaInscripcionDesc(Long idUsuario);

    List<Inscripcion> findByEvento_IdEvento(Long idEvento);

    @Query("""
            SELECT i FROM Inscripcion i
            JOIN FETCH i.usuario u
            WHERE i.evento.idEvento = :idEvento
              AND UPPER(TRIM(i.estado)) NOT IN ('INACTIVO', 'CANCELLED')
            ORDER BY u.apellidos ASC, u.nombres ASC
            """)
    List<Inscripcion> findActivasPorEventoConUsuario(@Param("idEvento") Long idEvento);

    @Query("""
            SELECT COUNT(i) FROM Inscripcion i
            WHERE i.evento.idEvento = :idEvento
              AND UPPER(TRIM(i.estado)) IN ('ASISTIO', 'PRESENTE', 'TARDIO')
            """)
    long countAsistenciasConfirmadasPorEvento(@Param("idEvento") Long idEvento);

    @Query("""
            SELECT i FROM Inscripcion i
            JOIN FETCH i.evento e
            LEFT JOIN FETCH e.usuarioCreador
            WHERE i.usuario.idUsuario = :idUsuario
            ORDER BY i.fechaInscripcion DESC
            """)
    List<Inscripcion> findMisInscripcionesConEvento(@Param("idUsuario") Long idUsuario);

    @Query("""
            SELECT COUNT(i) FROM Inscripcion i
            WHERE i.evento.idEvento = :idEvento
              AND UPPER(TRIM(i.estado)) NOT IN ('INACTIVO', 'CANCELLED')
            """)
    long countCuposOcupadosPorEvento(@Param("idEvento") Long idEvento);

    @Query("""
            SELECT i.evento.idEvento, COUNT(i)
            FROM Inscripcion i
            WHERE i.evento.idEvento IN :ids
              AND UPPER(TRIM(i.estado)) NOT IN ('INACTIVO', 'CANCELLED')
            GROUP BY i.evento.idEvento
            """)
    List<Object[]> countInscritosActivosAgrupadosPorEvento(@Param("ids") Collection<Long> ids);

    /**
     * Asistencias confirmadas: la entidad no expone columna {@code asistio}; el esquema usa
     * {@code estado = 'ASISTIO'} como única fuente de verdad hasta que exista migración con boolean.
     */
    @Query("""
            SELECT COUNT(i) FROM Inscripcion i
            WHERE UPPER(TRIM(i.estado)) IN ('ASISTIO', 'PRESENTE', 'TARDIO')
            """)
    long countAsistenciasPorEstado();

    @Query("""
            SELECT CAST(i.fechaInscripcion AS date), COUNT(i)
            FROM Inscripcion i
            WHERE UPPER(TRIM(i.estado)) IN ('ASISTIO', 'PRESENTE', 'TARDIO')
              AND i.fechaInscripcion >= :desde
              AND i.fechaInscripcion < :hasta
            GROUP BY CAST(i.fechaInscripcion AS date)
            ORDER BY CAST(i.fechaInscripcion AS date)
            """)
    List<Object[]> countAsistenciasPorDia(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query("""
            SELECT UPPER(TRIM(i.estado)), COUNT(i)
            FROM Inscripcion i
            GROUP BY UPPER(TRIM(i.estado))
            """)
    List<Object[]> countSolicitudesPorEstado();

    @Query("""
            SELECT COUNT(i) FROM Inscripcion i
            WHERE UPPER(TRIM(i.estado)) NOT IN ('INACTIVO', 'CANCELLED')
            """)
    long countInscripcionesActivasGlobales();
}
