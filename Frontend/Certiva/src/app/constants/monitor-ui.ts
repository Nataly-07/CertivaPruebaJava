import type { EstadoOperativoEvento } from '../Models/evento-dto';
import type { MonitorNivelAlerta } from '../Models/portal-dto';

export function etiquetaEstadoMonitor(estado?: EstadoOperativoEvento): string {
  switch (estado) {
    case 'EN_CURSO':
      return 'En curso';
    case 'PROXIMO':
      return 'Registro abierto';
    case 'EN_REVISION':
      return 'En revisión';
    case 'FINALIZADO_POR_TIEMPO':
      return 'Finalizado';
    case 'CERRADO_Y_CERTIFICADO':
      return 'Cerrado';
    case 'EVENT_CANCELLED':
      return 'Cancelado';
    default:
      return 'Por iniciar';
  }
}

export function etiquetaAlertaMonitor(nivel?: MonitorNivelAlerta): string {
  switch (nivel) {
    case 'CRITICO':
      return 'Crítico';
    case 'ADVERTENCIA':
      return 'Advertencia';
    default:
      return 'Normal';
  }
}
