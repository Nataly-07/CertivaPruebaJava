package com.certiva.api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearPagoDTO {

    private Double monto;

    private String metodoPago;

    private Long idInscripcion;
}
