import type { EstadoOperativoEvento } from './evento-dto';

export interface InscripcionPortalDTO {
  idInscripcion: number;
  estado: string;
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

export interface ProfesorPanelBannerDTO {
  sesionActivaHoy: boolean;
  idEvento?: number;
  nombreEvento?: string;
  fechaInicio?: string;
  fechaFin?: string;
  monitorNombre?: string;
  monitorApellidos?: string;
}

export interface ProfesorEventoTarjetaDTO {
  idEvento: number;
  nombreEvento: string;
  tipoEvento?: string;
  estadoOperativo?: EstadoOperativoEvento;
  inscritosActivos: number;
  asistenciasConfirmadas: number;
  porcentajeAsistenciaGlobal: number;
  fechaInicio?: string;
  fechaFin?: string;
  monitorNombre?: string;
  monitorApellidos?: string;
  requiereIniciarRevision?: boolean;
}

export interface ProfesorPanelDTO {
  totalEventos: number;
  totalInscritos: number;
  eventosActivos: number;
  eventosPorCertificar: number;
  accionesPendientes: number;
  banner: ProfesorPanelBannerDTO;
  enCurso: ProfesorEventoTarjetaDTO[];
  pendientesCierre: ProfesorEventoTarjetaDTO[];
  historial: ProfesorEventoTarjetaDTO[];
  eventos?: ProfesorEventoResumenDTO[];
}

export interface ProfesorEventoResumenDTO {
  idEvento: number;
  nombreEvento: string;
  tipoEvento?: string;
  activo: boolean;
  estadoOperativo?: string;
  inscritos: number;
  fechaInicio?: string;
  fechaFin?: string;
}

export interface EventoRevisionAlumnoDTO {
  idInscripcion: number;
  idResultadoEvaluacion?: number | null;
  nombres: string;
  apellidos: string;
  correo: string;
  estadoInscripcion: string;
  nota?: number | null;
  observaciones?: string | null;
  porcentajeAsistencia: number;
  asistenciaConfirmada: boolean;
  cumpleAsistenciaMinima?: boolean;
  elegibleCertificado: boolean;
  motivoNoElegible?: string | null;
}

export interface EventoRevisionPanelDTO {
  idEvento: number;
  nombreEvento: string;
  estadoOperativo?: EstadoOperativoEvento;
  totalInscritos: number;
  asistenciasRegistradas: number;
  elegiblesCertificado?: number;
  notaMinimaAprobacion?: number | null;
  porcentajeAsistenciaMinimo?: number | null;
  alumnos: EventoRevisionAlumnoDTO[];
}

export interface ProfesorAlumnoAsistenciaDTO {
  idInscripcion: number;
  nombres: string;
  apellidos: string;
  correo: string;
  numeroDocumento?: string;
  estadoInscripcion: string;
  asistenciaConfirmada: boolean;
  checkInsAcumulados?: number;
  sesionesTotales?: number;
  porcentajeAsistencia: number;
  tokenQr?: string;
}

export interface EventoAsistenciaEnVivoDTO {
  idEvento: number;
  nombreEvento: string;
  estadoOperativo?: EstadoOperativoEvento;
  totalInscritos: number;
  asistenciasConfirmadas: number;
  porcentajeAsistenciaGlobal: number;
  asistenciaPromedioSesionHoy?: number;
  alumnos: ProfesorAlumnoAsistenciaDTO[];
}

export interface GuardarRevisionAlumnoDTO {
  idInscripcion: number;
  nota?: number | null;
  observaciones?: string | null;
}

export interface RecursoAcademicoDTO {
  tipo: string;
  titulo: string;
  url: string;
}

export interface EventoContenidoAcademicoDTO {
  idEvento: number;
  nombreEvento: string;
  tipoEvento?: string;
  avisosReglas?: string | null;
  recursos: RecursoAcademicoDTO[];
  materialGuia?: string | null;
  retoTecnicoCentral?: string | null;
  premiosIncentivos?: string | null;
  criteriosEvaluacion?: string | null;
}

export interface GuardarEventoContenidoAcademicoDTO {
  avisosReglas?: string | null;
  recursos: RecursoAcademicoDTO[];
  materialGuia?: string | null;
  retoTecnicoCentral?: string | null;
  premiosIncentivos?: string | null;
  criteriosEvaluacion?: string | null;
}

export interface ProfesorParticipanteDTO {
  idInscripcion: number;
  nombres: string;
  apellidos: string;
  correo: string;
  numeroDocumento?: string;
  estadoInscripcion: string;
  perfilTecnico?: string;
}

export interface AsistenciaManualRequestDTO {
  eventId: number;
  idInscripcion?: number;
  studentId?: number;
  justification: string;
}
