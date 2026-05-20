package com.certiva.api.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.certiva.api.Entity.TipoEvento;

public interface TipoEventoRepository extends JpaRepository<TipoEvento, Long> {

    Optional<TipoEvento> findFirstByNombreIgnoreCase(String nombre);
}
