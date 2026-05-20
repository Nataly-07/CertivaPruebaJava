package com.certiva.api.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.certiva.api.Entity.Evaluacion;

public interface EvaluacionRepository extends JpaRepository<Evaluacion, Long> {

}
