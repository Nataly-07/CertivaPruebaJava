package com.certiva.api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolResumenDTO {

    private Long idRol;

    /** Nombre completo en BD, p. ej. {@code ROLE_ADMIN}. */
    private String nombre;

    /** Código corto sin prefijo {@code ROLE_}, p. ej. {@code ADMIN}. */
    private String codigo;
}
