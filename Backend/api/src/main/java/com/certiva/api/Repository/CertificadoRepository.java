package com.certiva.api.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.certiva.api.Entity.Certificado;

public interface CertificadoRepository extends JpaRepository<Certificado, Long> {

    Optional<Certificado> findByCodigoValidacion(String codigoValidacion);

    boolean existsByUsuario_IdUsuarioAndEvento_IdEvento(Long idUsuario, Long idEvento);

    boolean existsByCodigoValidacion(String codigoValidacion);

    Optional<Certificado> findFirstByUsuario_IdUsuarioAndEvento_IdEvento(Long idUsuario, Long idEvento);

    List<Certificado> findByUsuario_IdUsuarioOrderByFechaEmisionDesc(Long idUsuario);

    @Query("""
            SELECT CAST(c.fechaEmision AS date), COUNT(c)
            FROM Certificado c
            WHERE c.fechaEmision IS NOT NULL
              AND c.fechaEmision >= :desde
              AND c.fechaEmision < :hasta
            GROUP BY CAST(c.fechaEmision AS date)
            ORDER BY CAST(c.fechaEmision AS date)
            """)
    List<Object[]> countCertificadosPorDia(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
