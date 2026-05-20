export interface RolDTO {
  idRol: number;
  nombre: string;
  descripcion?: string;
  activo?: boolean;
  fechaCreacion?: string;
  fechaActualizacion?: string;
  /** Presente en {@code /api/roles/listar} y {@code /api/roles/registro} */
  codigo?: string;
}
