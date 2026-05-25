import { HttpErrorResponse } from '@angular/common/http';

/** Mensaje legible para errores HTTP del API (incluye backend caído). */
export function mensajeErrorHttp(err: unknown, fallback: string): string {
  if (!(err instanceof HttpErrorResponse)) {
    return fallback;
  }
  if (err.status === 0) {
    return 'No se puede conectar con el servidor API (http://localhost:8080). Verifique que el backend Spring Boot esté en ejecución.';
  }
  const body = err.error;
  if (body && typeof body === 'object' && 'mensaje' in body && typeof body.mensaje === 'string') {
    return body.mensaje;
  }
  if (typeof body === 'string' && body.trim()) {
    return body;
  }
  return fallback;
}
