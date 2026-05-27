package com.certiva.api.Repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.certiva.api.Entity.Auditoria;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    List<Auditoria> findTop100ByOrderByFechaDesc();

    @Query("""
            SELECT a FROM Auditoria a
            LEFT JOIN FETCH a.usuario
            WHERE (:accion IS NULL OR UPPER(TRIM(a.accion)) = UPPER(TRIM(:accion)))
              AND (:desde IS NULL OR a.fecha >= :desde)
              AND (:hasta IS NULL OR a.fecha <= :hasta)
              AND (:busqueda IS NULL OR :busqueda = '' OR
                   LOWER(a.descripcion) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                   LOWER(a.accion) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                   LOWER(COALESCE(a.entidadAfectada, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')))
            ORDER BY a.fecha DESC
            """)
    List<Auditoria> buscarConFiltros(
            @Param("accion") String accion,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("busqueda") String busqueda,
            Pageable pageable);
}
