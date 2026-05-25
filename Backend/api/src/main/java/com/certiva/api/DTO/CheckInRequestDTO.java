package com.certiva.api.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckInRequestDTO {

    @NotBlank(message = "El código QR / UUID es obligatorio")
    private String codigo;

    /** PRESENTE (default) o TARDIO. */
    private String tipoAsistencia;
}
