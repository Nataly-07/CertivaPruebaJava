import { environment } from '../../environments/environment';

function apiBaseSinApi(): string {
  return environment.API_URL.replace(/\/api\/?$/, '');
}

function limpiarEntrada(ruta: string): string {
  return ruta.trim().replace(/\s+/g, '');
}

/** Es ruta relativa de archivo subido al servidor (EventoArchivoStorage). */
export function esRutaArchivoSubido(r: string): boolean {
  const rel = r.replace(/^\/+/, '');
  return /^(imagenes|pensum)\//i.test(rel) || /^uploads\//i.test(rel);
}

function pareceHostWeb(valor: string): boolean {
  try {
    const probe = valor.includes('://') ? valor : `https://${valor}`;
    const u = new URL(probe);
    const host = u.hostname;
    if (!host) {
      return false;
    }
    return host.includes('.') || host === 'localhost' || /^\d{1,3}(\.\d{1,3}){3}$/.test(host);
  } catch {
    return false;
  }
}

/** Convierte enlaces de vista previa (Drive, Dropbox) a URL directa para &lt;img&gt;. */
export function convertirEnlaceCompartidoImagen(url: string): string {
  const drive =
    url.match(/drive\.google\.com\/file\/d\/([a-zA-Z0-9_-]+)/i) ??
    url.match(/drive\.google\.com\/open\?id=([a-zA-Z0-9_-]+)/i);
  if (drive) {
    return `https://drive.google.com/uc?export=view&id=${drive[1]}`;
  }
  if (/dropbox\.com/i.test(url)) {
    let out = url.replace(/\?dl=0\b/, '?raw=1').replace(/&dl=0\b/, '&raw=1');
    if (!/[\?&]raw=1\b/.test(out)) {
      out += out.includes('?') ? '&raw=1' : '?raw=1';
    }
    return out;
  }
  return url;
}

/**
 * Normaliza la URL/ruta antes de guardar en BD.
 * Las URLs externas quedan con https://; las rutas locales se conservan relativas.
 */
export function normalizarUrlImagenParaGuardar(ruta: string | null | undefined): string | null {
  if (!ruta?.trim()) {
    return null;
  }
  let r = limpiarEntrada(ruta);

  if (r.startsWith('data:')) {
    return r;
  }
  if (r.startsWith('//')) {
    return convertirEnlaceCompartidoImagen(`https:${r}`);
  }
  if (/^https?:\/\//i.test(r)) {
    return convertirEnlaceCompartidoImagen(r);
  }

  const apiBase = apiBaseSinApi();
  if (r.startsWith(apiBase)) {
    r = r.slice(apiBase.length).replace(/^\/+/, '');
  }
  if (r.startsWith('/uploads/')) {
    return r.slice('/uploads/'.length);
  }
  if (r.startsWith('uploads/')) {
    return r.slice('uploads/'.length);
  }

  if (esRutaArchivoSubido(r)) {
    return r.replace(/^\/+/, '');
  }

  if (pareceHostWeb(r)) {
    return convertirEnlaceCompartidoImagen(`https://${r.replace(/^\/+/, '')}`);
  }

  return r.replace(/^\/+/, '');
}

/** Resuelve URL absoluta de imagen promocional (ruta local /uploads o URL externa). */
export function resolverUrlImagenEvento(ruta: string | null | undefined): string | null {
  const guardada = normalizarUrlImagenParaGuardar(ruta);
  if (!guardada) {
    return null;
  }

  if (guardada.startsWith('data:') || /^https?:\/\//i.test(guardada)) {
    return guardada;
  }

  const apiBase = apiBaseSinApi();
  const rel = guardada.replace(/^\/+/, '');

  if (esRutaArchivoSubido(rel)) {
    return `${apiBase}/uploads/${rel.replace(/^uploads\//i, '')}`;
  }

  if (pareceHostWeb(rel)) {
    return convertirEnlaceCompartidoImagen(`https://${rel}`);
  }

  return `${apiBase}/${rel}`;
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
