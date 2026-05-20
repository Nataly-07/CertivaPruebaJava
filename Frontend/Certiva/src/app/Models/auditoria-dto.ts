export interface AuditoriaResumenDTO {
  idAuditoria: number;
  accion: string;
  entidadAfectada?: string;
  descripcion?: string;
  ip?: string;
  fecha: string;
  idUsuario?: number;
}
