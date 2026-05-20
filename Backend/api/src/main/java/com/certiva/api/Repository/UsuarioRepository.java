package com.certiva.api.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.certiva.api.Entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Conteo por rol en base de datos (GROUP BY {@code r.nombre}); el servicio traduce a etiquetas en español.
     */
    @Query("SELECT r.nombre, COUNT(DISTINCT u.idUsuario) FROM Usuario u JOIN u.roles r GROUP BY r.nombre")
    List<Object[]> countUsuariosPorNombreRol();

    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    boolean existsByCorreoAndIdUsuarioNot(String correo, Long idUsuario);

    boolean existsByNumeroDocumento(String numeroDocumento);

    boolean existsByNumeroDocumentoAndIdUsuarioNot(String numeroDocumento, Long idUsuario);

    @Query("SELECT u.correo FROM Usuario u WHERE u.correo IN :correos")
    List<String> findCorreosExistentes(@Param("correos") Collection<String> correos);

    @Query("SELECT u.numeroDocumento FROM Usuario u WHERE u.numeroDocumento IN :documentos")
    List<String> findDocumentosExistentes(@Param("documentos") Collection<String> documentos);

    @Query("SELECT COUNT(DISTINCT u.idUsuario) FROM Usuario u JOIN u.roles r WHERE r.nombre = :nombreRol")
    long countUsuariosConRol(@Param("nombreRol") String nombreRol);

    @Query("SELECT COUNT(DISTINCT u.idUsuario) FROM Usuario u JOIN u.roles r WHERE r.nombre = :nombreRol AND u.idUsuario <> :excluirId")
    long countUsuariosConRolExceptoUsuario(@Param("nombreRol") String nombreRol, @Param("excluirId") Long excluirId);

    @Query("""
            SELECT DISTINCT u FROM Usuario u JOIN u.roles r
            WHERE r.nombre = :nombreRol AND u.estado = true
              AND (:q IS NULL OR :q = '' OR LOWER(u.nombres) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(u.apellidos) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(u.correo) LIKE LOWER(CONCAT('%', :q, '%'))
                OR u.numeroDocumento LIKE CONCAT('%', :q, '%'))
            ORDER BY u.nombres, u.apellidos
            """)
    List<Usuario> buscarActivosPorRol(@Param("nombreRol") String nombreRol, @Param("q") String q);
}
