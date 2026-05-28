import { environment } from '../../environments/environment';

/** Resuelve URL absoluta de imagen promocional (ruta local /uploads o URL externa). */
export function resolverUrlImagenEvento(ruta: string | null | undefined): string | null {
  if (!ruta?.trim()) {
    return null;
  }
  const r = ruta.trim();
  if (r.startsWith('http://') || r.startsWith('https://') || r.startsWith('data:')) {
    return r;
  }
  const apiBase = environment.API_URL.replace(/\/api\/?$/, '');
  return `${apiBase}${r.startsWith('/') ? r : `/${r}`}`;
}

export function placeholderGradienteTipo(tipo: string | null | undefined): string {
  const gradients: Record<string, string> = {
    CURSO: 'linear-gradient(135deg, #1e3a5f 0%, #3b82f6 100%)',
    HACKATHON: 'linear-gradient(135deg, #4c1d95 0%, #7c3aed 100%)',
    TALLER: 'linear-gradient(135deg, #134e4a 0%, #14b8a6 100%)',
    FERIA: 'linear-gradient(135deg, #78350f 0%, #f59e0b 100%)',
  };
  return gradients[tipo ?? ''] ?? gradients['CURSO'];
}
