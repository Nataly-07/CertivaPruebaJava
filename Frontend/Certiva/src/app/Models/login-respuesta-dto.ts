import { UsuarioDTO } from './usuario-dto';

export interface LoginRespuestaDTO {
  mensaje: string;
  token: string;
  usuarioDTO: UsuarioDTO;
}
