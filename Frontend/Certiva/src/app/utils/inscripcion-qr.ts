import { environment } from '../../environments/environment';

const INSCRIPCION_ID_RE = /(?:\?|&|#)inscripcionId=(\d+)/i;

/** URL que se codifica en el QR (endpoint de validación + id de inscripción). */
export function contenidoQrInscripcion(idInscripcion: number, tokenQr?: string | null): string {
  if (tokenQr?.trim()) {
    return tokenQr.trim();
  }
  const base = environment.API_URL.replace(/\/api\/?$/i, '');
  return `${base}/api/asistencias/validar?inscripcionId=${idInscripcion}`;
}

/** Normaliza lo escaneado/pegado para el check-in del monitor. */
export function normalizarCodigoQr(entrada: string): string {
  const raw = entrada?.trim() ?? '';
  if (!raw) {
    return '';
  }
  const match = raw.match(INSCRIPCION_ID_RE);
  if (match?.[1]) {
    return match[1];
  }
  return raw;
}

export function extraerInscripcionId(entrada: string): number | null {
  const norm = normalizarCodigoQr(entrada);
  if (/^\d+$/.test(norm)) {
    return Number(norm);
  }
  return null;
}
