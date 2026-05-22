export interface InscripcionPortalDTO {
  idInscripcion: number;
  estado: string;
  /** Contenido del QR: URL de validación con idInscripcion. */
  tokenQr?: string;
  fechaInscripcion?: string;
  idEvento: number;
  nombreEvento: string;
  tipoEvento?: string;
  modalidad?: string;
  instructorNombre?: string;
  enlaceVirtual?: string;
  fechaInicio?: string;
  fechaFin?: string;
  fase: 'INSCRITO' | 'EN_CURSO' | 'FINALIZADO';
  sesionesTotales?: number;
  sesionesAsistidas?: number;
  porcentajeProgreso?: number;
  puedeDescargarCertificado: boolean;
  idCertificado?: number;
  motivoCertificadoPendiente?: string;
}

export interface CertificadoPortalDTO {
  idCertificado: number;
  codigoValidacion: string;
  nombreEvento: string;
  tipoEvento?: string;
  fechaEmision?: string;
  puedeDescargar: boolean;
  motivoPendiente?: string;
}

export interface ProfesorPanelDTO {
  totalEventos: number;
  totalInscritos: number;
  eventosActivos: number;
  eventos: ProfesorEventoResumenDTO[];
}

export interface ProfesorEventoResumenDTO {
  idEvento: number;
  nombreEvento: string;
  tipoEvento?: string;
  activo: boolean;
  inscritos: number;
  fechaInicio?: string;
  fechaFin?: string;
}
