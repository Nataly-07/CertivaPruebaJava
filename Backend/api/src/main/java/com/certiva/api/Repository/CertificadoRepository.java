package com.certiva.api.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.certiva.api.Entity.Certificado;
import com.certiva.api.enums.EstadoCertificado;

public interface CertificadoRepository extends JpaRepository<Certificado, Long> {

    Optional<Certificado> findByCodigoValidacion(String codigoValidacion);

    boolean existsByUsuario_IdUsuarioAndEvento_IdEvento(Long idUsuario, Long idEvento);

    boolean existsByCodigoValidacion(String codigoValidacion);

    Optional<Certificado> findFirstByUsuario_IdUsuarioAndEvento_IdEvento(Long idUsuario, Long idEvento);

    List<Certificado> findByUsuario_IdUsuarioOrderByFechaEmisionDesc(Long idUsuario);

    long countByEstado(EstadoCertificado estado);

    Optional<Certificado> findTopByEstadoOrderByFechaEmisionDesc(EstadoCertificado estado);

    @Query("""
            SELECT DISTINCT c FROM Certificado c
            JOIN FETCH c.usuario u
            JOIN FETCH c.evento e
            WHERE (:idEvento IS NULL OR e.idEvento = :idEvento)
              AND (:busqueda IS NULL OR :busqueda = '' OR
                   LOWER(c.codigoValidacion) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                   LOWER(u.nombres) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                   LOWER(u.apellidos) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                   LOWER(u.numeroDocumento) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                   LOWER(CONCAT(u.nombres, ' ', u.apellidos)) LIKE LOWER(CONCAT('%', :busqueda, '%')))
            ORDER BY c.fechaEmision DESC
            """)
    List<Certificado> buscarVistaAdmin(
            @Param("busqueda") String busqueda,
            @Param("idEvento") Long idEvento);

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

    @Query("""
            SELECT e.idEvento, e.nombreEvento FROM Certificado c
            JOIN c.evento e
            GROUP BY e.idEvento, e.nombreEvento
            ORDER BY e.nombreEvento
            """)
    List<Object[]> listarEventosConCertificados();
}
