package com.certiva.api.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.certiva.api.Entity.RespuestaFormulario;

public interface RespuestaFormularioRepository extends JpaRepository<RespuestaFormulario, Long> {

    List<RespuestaFormulario> findByInscripcion_IdInscripcionOrderByIdRespuestaAsc(Long idInscripcion);
}
