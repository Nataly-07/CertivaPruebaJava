package com.certiva.api.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.certiva.api.Entity.CampoFormulario;

public interface CampoFormularioRepository extends JpaRepository<CampoFormulario, Long> {

    List<CampoFormulario> findByEvento_IdEventoOrderByIdCampoAsc(Long idEvento);

    void deleteByEvento_IdEvento(Long idEvento);
}
