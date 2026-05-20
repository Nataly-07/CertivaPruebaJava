export interface DashboardDTO {
  totalUsuarios: number;
  eventosActivos: number;
  asistenciasTotales: number;
  certificadosEmitidos: number;
  distribucionRoles: Record<string, number>;
}

export interface DashboardActivityPointDTO {
  fecha: string;
  asistencias: number;
  certificados: number;
}

export interface DashboardActivityDTO {
  rangoDias: number;
  puntos: DashboardActivityPointDTO[];
}
