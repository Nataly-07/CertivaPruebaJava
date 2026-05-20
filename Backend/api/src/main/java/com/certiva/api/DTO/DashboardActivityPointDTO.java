package com.certiva.api.DTO;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardActivityPointDTO {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha;

    private Long asistencias;

    private Long certificados;
}
