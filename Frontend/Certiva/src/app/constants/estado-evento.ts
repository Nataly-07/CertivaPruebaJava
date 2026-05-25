export type EstadoOperativoEvento =
  | 'PROXIMO'
  | 'EN_CURSO'
  | 'FINALIZADO_POR_TIEMPO'
  | 'EN_REVISION'
  | 'CERRADO_Y_CERTIFICADO'
  | 'EVENT_CANCELLED';

export const ETIQUETAS_ESTADO_EVENTO: Record<EstadoOperativoEvento, string> = {
  PROXIMO: 'Próximo',
  EN_CURSO: 'En curso',
  FINALIZADO_POR_TIEMPO: 'Finalizado por tiempo',
  EN_REVISION: 'En revisión',
  CERRADO_Y_CERTIFICADO: 'Cerrado y certificado',
  EVENT_CANCELLED: 'Cancelado',
};

export function etiquetaEstadoEvento(codigo: string | null | undefined): string {
  if (!codigo) return '';
  return ETIQUETAS_ESTADO_EVENTO[codigo as EstadoOperativoEvento] ?? codigo;
}
