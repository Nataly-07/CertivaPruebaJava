package com.certiva.api.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.certiva.api.Entity.ResultadoEvaluacion;

import java.util.Optional;

public interface ResultadoEvaluacionRepository extends JpaRepository<ResultadoEvaluacion, Long> {

    Optional<ResultadoEvaluacion> findFirstByInscripcion_IdInscripcionOrderByIdDesc(Long idInscripcion);

}
