package com.certiva.api.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.certiva.api.Entity.Rol;
import com.certiva.api.Entity.TipoDocumento;

public interface TipoDocumentoRepository extends JpaRepository<TipoDocumento, Long> {

  /*   Optional<Rol> findByNombre(String string); */

    Optional<TipoDocumento> findByNombre(String nombre);
}
