import { ModalidadEvento, TipoEventoEnum } from './evento-dto';

/** Respuesta de GET /api/public/eventos/catalogo */
export interface EventoPublico {
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
