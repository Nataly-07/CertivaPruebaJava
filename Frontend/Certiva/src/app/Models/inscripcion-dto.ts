import { RespuestaCampoDTO } from './evento-dto';

export interface CrearInscripcionDTO {
  idUsuario: number;
  idEvento: number;
  tokenQr?: string | null;
  respuestasCampos?: RespuestaCampoDTO[];
}

export interface InscripcionDTO {
  idInscripcion: number;
  estado?: string | null;
  pagoRealizado?: boolean | null;
  fechaInscripcion?: string | null;
  idUsuario?: number | null;
  idEvento?: number | null;
  tokenQr?: string | null;
  respuestasCampos?: RespuestaCampoDTO[];
}
