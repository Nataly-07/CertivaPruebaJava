package com.certiva.api.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class DetalleHackathonDTO {

    @NotBlank
    private String retoTecnicoCentral;

    @NotNull
    @Positive
    private Integer minIntegrantes;

    @NotNull
    @Positive
    private Integer maxIntegrantes;

    private String premiosIncentivos;
}
