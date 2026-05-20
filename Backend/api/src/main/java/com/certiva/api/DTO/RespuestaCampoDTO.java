package com.certiva.api.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RespuestaCampoDTO {

    @NotNull
    private Long idCampo;

    @Size(max = 2048)
    private String valor;
}
