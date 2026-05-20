package com.certiva.api.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoCupoVerificacionDTO {

    private long inscritosActivos;

    private Integer aforoMaximo;

    private boolean hayCupoDisponible;
}
