package com.certiva.api.Service;

import com.certiva.api.DTO.DashboardActivityDTO;
import com.certiva.api.DTO.DashboardDTO;

public interface DashboardService {

    DashboardDTO obtenerEstadisticas(String ipRemota);

    DashboardActivityDTO obtenerActividad(int dias);
}
