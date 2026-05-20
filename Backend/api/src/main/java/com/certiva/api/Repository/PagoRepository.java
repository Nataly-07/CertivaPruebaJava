package com.certiva.api.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.certiva.api.Entity.Pago;

public interface PagoRepository extends JpaRepository<Pago, Long> {

}
