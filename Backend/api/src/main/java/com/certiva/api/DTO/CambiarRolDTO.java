package com.certiva.api.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambiarRolDTO {

    @NotNull(message = "El rol es obligatorio")
    private Long idRol;
}
