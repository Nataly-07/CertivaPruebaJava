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

export type EstadoCertificado = 'VALIDO' | 'REVOCADO';

export interface CertificadosAdminKpiDTO {
  totalEmitidos: number;
  eventosClausurados: number;
  tasaAprobacion: number;
  ultimaEmisionTexto: string;
  ultimoEventoNombre?: string | null;
}

export interface CertificadoAdminFilaDTO {
  idCertificado: number;
  codigoValidacion: string;
  codigoMostrar: string;
  nombreParticipante: string;
  numeroDocumento?: string;
  idEvento: number;
  nombreEvento: string;
  fechaEmision: string;
  estado: EstadoCertificado;
  tienePdf: boolean;
}

export interface EventoOpcionFiltroDTO {
  idEvento: number;
  nombreEvento: string;
}

export interface CertificadosAdminVistaDTO {
  kpis: CertificadosAdminKpiDTO;
  certificados: CertificadoAdminFilaDTO[];
  eventosFiltro: EventoOpcionFiltroDTO[];
}
