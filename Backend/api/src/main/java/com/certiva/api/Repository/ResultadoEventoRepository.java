package com.certiva.api.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.certiva.api.Entity.ResultadoEvento;

public interface ResultadoEventoRepository extends JpaRepository<ResultadoEvento, Long> {

}
