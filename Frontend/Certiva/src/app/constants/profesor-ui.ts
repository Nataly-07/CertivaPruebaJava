import { etiquetaTipoEvento } from './ui-labels';

export { etiquetaTipoEvento };

const ETIQUETAS_PROFESOR: Record<string, string> = {
  PROXIMO: 'Inscripciones',
  EN_CURSO: 'En curso',
  FINALIZADO_POR_TIEMPO: 'Por certificar',
  EN_REVISION: 'Por certificar',
  CERRADO_Y_CERTIFICADO: 'Clausurado',
  EVENT_CANCELLED: 'Cancelado',
};

export function etiquetaEstadoProfesor(codigo: string | null | undefined): string {
  if (!codigo) return '—';
  return ETIQUETAS_PROFESOR[codigo] ?? codigo;
}
