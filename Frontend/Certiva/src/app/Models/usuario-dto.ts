export interface UsuarioDTO {
  idUsuario: number;
  nombres: string;
  apellidos: string;
  numeroDocumento: string;
  correo: string;
  telefono?: string;
  estado: boolean | string;
  fechaRegistro: string;
  idRol?: number;
  idTipoDocumento?: number;
  rol?: {
    idRol: number;
    nombre: string;
    codigo?: string;
  };
  tipoDocumento?: {
    idTipoDocumento: number;
    nombre: string;
  };
}
