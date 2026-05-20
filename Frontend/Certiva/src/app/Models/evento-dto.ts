export type ModalidadEvento = 'PRESENCIAL' | 'VIRTUAL' | 'HIBRIDO';

export type TipoEventoEnum = 'CURSO' | 'HACKATHON' | 'TALLER' | 'FERIA';

export type TipoDatoCampo = 'TEXTO' | 'NUMERO' | 'SELECT' | 'CHECKBOX' | 'URL' | 'IMAGEN';

export type NivelAcademico = 'BASICO' | 'INTERMEDIO' | 'AVANZADO';

export type CategoriaExhibicionFeria =
  | 'SOFTWARE'
  | 'HARDWARE'
  | 'INTELIGENCIA_ARTIFICIAL'
  | 'INNOVACION_SOCIAL';

export interface UsuarioStaffDTO {
  idUsuario: number;
  nombres: string;
  apellidos: string;
  correo: string;
  numeroDocumento: string;
  codigoRol: string;
}

export interface DetalleCursoDTO {
  nivelAcademico: NivelAcademico;
  notaMinimaAprobacion: number;
  porcentajeAsistenciaMinimo: number;
}

export interface DetalleHackathonDTO {
  retoTecnicoCentral: string;
  minIntegrantes: number;
  maxIntegrantes: number;
  premiosIncentivos?: string | null;
}

export interface DetalleFeriaDTO {
  categoriaExhibicion: CategoriaExhibicionFeria;
  stackTecnologico: string;
  criteriosEvaluacion: string;
}

export interface DetalleTallerDTO {
  materialGuia?: string | null;
}

export interface RespuestaCampoDTO {
  idCampo: number;
  valor: string;
}

export interface CampoFormularioDTO {
  idCampo?: number;
  idEvento?: number | null;
  etiqueta: string;
  tipoDato: TipoDatoCampo;
  esObligatorio: boolean;
  opciones?: string | null;
}

export interface CrearCampoFormularioDTO {
  etiqueta: string;
  tipoDato: TipoDatoCampo;
  esObligatorio: boolean;
  opciones?: string | null;
}

export interface EventoDTO {
  idEvento: number;
  nombreEvento: string;
  descripcion?: string | null;
  tipoEvento: TipoEventoEnum;
  modalidad: ModalidadEvento;
  fechaInicio: string;
  fechaFin: string;
  ubicacion?: string | null;
  enlaceVirtual?: string | null;
  aforoMaximo: number;
  intensidadHoraria: number;
  precio: number;
  gratuito?: boolean;
  codigoDifusion?: string | null;
  urlInscripcionPublica?: string | null;
  rutaImagenPromocional?: string | null;
  rutaPensum?: string | null;
  textoDiploma?: string | null;
  firmaDigitalProfesor?: string | null;
  estado?: boolean | null;
  idUsuarioCreador?: number | null;
  idsProfesoresColaboradores?: number[];
  idsMonitoresAsignados?: number[];
  profesoresColaboradores?: UsuarioStaffDTO[];
  monitoresAsignados?: UsuarioStaffDTO[];
  detalleCurso?: DetalleCursoDTO | null;
  detalleHackathon?: DetalleHackathonDTO | null;
  detalleFeria?: DetalleFeriaDTO | null;
  detalleTaller?: DetalleTallerDTO | null;
  camposPersonalizados?: CampoFormularioDTO[];
}

export interface CrearEventoDTO {
  nombreEvento: string;
  descripcion?: string | null;
  tipoEvento: TipoEventoEnum;
  modalidad: ModalidadEvento;
  fechaInicio: string;
  fechaFin: string;
  ubicacion?: string | null;
  enlaceVirtual?: string | null;
  aforoMaximo: number;
  intensidadHoraria: number;
  precio: number;
  textoDiploma?: string | null;
  firmaDigitalProfesor?: string | null;
  idsProfesoresColaboradores?: number[];
  idsMonitoresAsignados?: number[];
  detalleCurso?: DetalleCursoDTO | null;
  detalleHackathon?: DetalleHackathonDTO | null;
  detalleFeria?: DetalleFeriaDTO | null;
  detalleTaller?: DetalleTallerDTO | null;
  camposPersonalizados?: CrearCampoFormularioDTO[];
}

export interface EventoCatalogoPublicoDTO {
  idEvento: number;
  nombreEvento: string;
  descripcion?: string | null;
  tipoEvento: TipoEventoEnum;
  area: string;
  modalidad: ModalidadEvento;
  fechaInicio: string;
  fechaFin: string;
  ubicacion?: string | null;
  enlaceVirtual?: string | null;
  aforoMaximo?: number | null;
  precio: number;
  gratuito?: boolean;
  rutaImagenPromocional?: string | null;
  instructorNombres?: string | null;
  instructorApellidos?: string | null;
  instructorRolEtiqueta?: string | null;
  inscritosActivos: number;
  hayCupoDisponible: boolean;
}

export interface EventoPublicoDTO {
  idEvento: number;
  nombreEvento: string;
  descripcion?: string | null;
  tipoEvento: TipoEventoEnum;
  modalidad: ModalidadEvento;
  fechaInicio: string;
  fechaFin: string;
  ubicacion?: string | null;
  enlaceVirtual?: string | null;
  aforoMaximo: number;
  intensidadHoraria: number;
  precio: number;
  gratuito?: boolean;
  rutaImagenPromocional?: string | null;
  codigoDifusion?: string | null;
  urlInscripcionPublica?: string | null;
  hayCupoDisponible: boolean;
  camposPersonalizados?: CampoFormularioDTO[];
}

export interface EventoResumenTipoDTO {
  tipo: TipoEventoEnum;
  totalEventos: number;
  porcentajeOcupacion: number;
}

export interface EventoFilaAdminDTO {
  idEvento: number;
  nombreEvento: string;
  tipoEvento: TipoEventoEnum;
  modalidad: ModalidadEvento;
  instructorPrincipal: string;
  inscritosActivos: number;
  aforoMaximo: number;
  estado?: boolean | null;
  fechaInicio?: string;
  fechaFin?: string;
}

export interface EventoCupoVerificacionDTO {
  inscritosActivos: number;
  aforoMaximo: number | null;
  hayCupoDisponible: boolean;
}
