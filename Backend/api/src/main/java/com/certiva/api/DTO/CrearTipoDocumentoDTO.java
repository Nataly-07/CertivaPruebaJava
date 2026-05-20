package com.certiva.api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearTipoDocumentoDTO {

    private String nombre;

    private String tipoDocumento;
}
