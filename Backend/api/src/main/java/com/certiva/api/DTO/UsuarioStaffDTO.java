package com.certiva.api.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioStaffDTO {

    private Long idUsuario;
    private String nombres;
    private String apellidos;
    private String correo;
    private String numeroDocumento;
    private String codigoRol;
}
