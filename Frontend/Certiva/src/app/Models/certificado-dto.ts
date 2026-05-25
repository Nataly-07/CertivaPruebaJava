export interface CertificadoDTO {
  idCertificado: number;
  tipoCertificado: string;
  codigoValidacion: string;
  fechaEmision: string;
  idUsuario: number;
  idEvento: number;
}

export interface CertificadoVerificacionDTO {
  valido: boolean;
  codigoValidacion?: string;
  nombreParticipante?: string;
  tituloEvento?: string;
  fechaEmision?: string;
  mensaje: string;
}

export interface CheckInRespuestaDTO {
  mensaje: string;
  idInscripcion?: number;
  estadoInscripcion?: string;
  codigoCertificado?: string;
  idCertificado?: number;
  /** Motivo si el certificado no se emite hasta el cierre del evento. */
  certificadoPendienteMotivo?: string;
}
