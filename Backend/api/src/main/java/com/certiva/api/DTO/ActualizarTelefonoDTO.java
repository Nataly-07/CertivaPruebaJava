package com.certiva.api.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ActualizarTelefonoDTO {

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(min = 7, max = 20, message = "Ingrese un teléfono válido (7 a 20 caracteres)")
    private String telefono;
}
