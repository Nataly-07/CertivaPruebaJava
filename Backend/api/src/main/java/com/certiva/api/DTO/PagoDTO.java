package com.certiva.api.DTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoDTO {

    private Long idPago;

    private Double monto;

    private String estado;

    private String metodoPago;

    private LocalDateTime fechaPago;

    private Long idInscripcion;
}
