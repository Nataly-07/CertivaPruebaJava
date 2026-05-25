package com.certiva.api.Repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.certiva.api.Entity.Evento;
import com.certiva.api.enums.ModalidadEvento;
import com.certiva.api.enums.TipoEventoEnum;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/* public interface EventoRepository extends JpaRepository<Evento, Long> */ 
  public interface EventoRepository extends
        JpaRepository<Evento, Long>,
        JpaSpecificationExecutor<Evento> {

    long countByEstadoTrue();

    List<Evento> findByCatalogoTipoEventoIsNull();

    @Query("SELECT COUNT(e) FROM Evento e WHERE e.fechaInicio <= :ahora AND e.fechaFin >= :ahora AND e.estado = true")
    long countEventosActivosEn(@Param("ahora") LocalDateTime ahora);

/*     @Query("""
            SELECT DISTINCT e FROM Evento e LEFT JOIN FETCH e.usuarioCreador
            WHERE (:activo IS NULL OR e.estado = :activo)
              AND (:modalidad IS NULL OR e.modalidad = :modalidad)
              AND (:tipo IS NULL OR e.tipoEvento = :tipo)
              AND (:desde IS NULL OR e.fechaInicio >= :desde)
              AND (:hasta IS NULL OR e.fechaInicio <= :hasta)
            ORDER BY e.fechaInicio DESC
            """)
    List<Evento> buscarConFiltros(@Param("activo") Boolean activo,
                                  @Param("modalidad") ModalidadEvento modalidad,
                                  @Param("tipo") TipoEventoEnum tipo,
                                  @Param("desde") LocalDateTime desde,
                                  @Param("hasta") LocalDateTime hasta); */

    @Query("""
            SELECT DISTINCT e FROM Evento e LEFT JOIN FETCH e.usuarioCreador
            WHERE (:activo IS NULL OR e.estado = :activo)
              AND (:modalidad IS NULL OR e.modalidad = :modalidad)
              AND (:tipo IS NULL OR e.tipoEvento = :tipo)
              AND (:desde IS NULL OR e.fechaInicio >= :desde)
              AND (:hasta IS NULL OR e.fechaInicio <= :hasta)
            ORDER BY e.fechaInicio DESC
            """)
    List<Evento> buscarConFiltros(
            @Param("activo") Boolean activo,
            @Param("modalidad") ModalidadEvento modalidad,
            @Param("tipo") TipoEventoEnum tipo,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    @Query("""
            SELECT e FROM Evento e
            WHERE e.estado = true
              AND e.fechaInicio >= :desde
              AND e.fechaInicio < :hastaExclusivo
            ORDER BY e.fechaInicio ASC
            """)
    List<Evento> findActivosPorRangoFechaInicio(@Param("desde") LocalDateTime desde,
                                                @Param("hastaExclusivo") LocalDateTime hastaExclusivo);

    @Query("""
            SELECT DISTINCT e FROM Evento e
            LEFT JOIN FETCH e.usuarioCreador
            LEFT JOIN FETCH e.profesoresColaboradores
            WHERE e.idEvento = :id
            """)
    java.util.Optional<Evento> findByIdConProfesores(@Param("id") Long id);

    @Query("""
            SELECT DISTINCT e FROM Evento e
            LEFT JOIN FETCH e.monitoresAsignados
            WHERE e.idEvento = :id
            """)
    java.util.Optional<Evento> findByIdConMonitores(@Param("id") Long id);

    java.util.Optional<Evento> findByCodigoDifusionAndEstadoTrue(String codigoDifusion);

    boolean existsByCodigoDifusion(String codigoDifusion);

    List<Evento> findByUsuarioCreador_IdUsuarioOrderByFechaInicioDesc(Long idUsuario);

    @Query("""
            SELECT DISTINCT e.idEvento FROM Evento e
            JOIN e.profesoresColaboradores p
            WHERE p.idUsuario = :idUsuario
            """)
    List<Long> findIdsEventoDondeEsColaborador(@Param("idUsuario") Long idUsuario);

    /**
     * Misma carga que {@link #buscarConFiltros}: sin FETCH de colecciones ManyToMany
     * (evita duplicados y errores al hidratar subclases JOINED en el listado admin).
     */
    @Query("""
            SELECT DISTINCT e FROM Evento e
            LEFT JOIN FETCH e.usuarioCreador
            WHERE (:activo IS NULL OR e.estado = :activo)
              AND (:modalidad IS NULL OR e.modalidad = :modalidad)
              AND (:tipo IS NULL OR e.tipoEvento = :tipo)
              AND (:desde IS NULL OR e.fechaInicio >= :desde)
              AND (:hasta IS NULL OR e.fechaInicio <= :hasta)
            ORDER BY e.fechaInicio DESC
            """)
    List<Evento> buscarConFiltrosParaVistaAdmin(
            @Param("activo") Boolean activo,
            @Param("modalidad") ModalidadEvento modalidad,
            @Param("tipo") TipoEventoEnum tipo,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);
}
