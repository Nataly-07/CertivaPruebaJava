package com.certiva.api.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.certiva.api.Entity.Auditoria;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    List<Auditoria> findTop100ByOrderByFechaDesc();

}
