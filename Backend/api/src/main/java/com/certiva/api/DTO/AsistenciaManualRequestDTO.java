package com.certiva.api.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsistenciaManualRequestDTO {

    @NotNull
    private Long eventId;

    /** Puede venir idInscripcion directo o studentId para resolver la inscripción activa. */
    private Long idInscripcion;
    private Long studentId;

    @NotBlank
    private String justification;
}
