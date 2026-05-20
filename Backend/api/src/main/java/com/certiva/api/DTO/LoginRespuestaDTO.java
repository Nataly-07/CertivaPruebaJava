package com.certiva.api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRespuestaDTO {

    private String mensaje;

    private String token;

    private UsuarioDTO usuarioDTO;
}
