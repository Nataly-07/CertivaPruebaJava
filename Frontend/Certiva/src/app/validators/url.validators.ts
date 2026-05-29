import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/** Límite para enlaces de reunión (Meet, Zoom, etc.). */
export const URL_MAX_LENGTH = 2048;

/** Banner promocional: URLs de CDN pueden superar 2 KB (Google, S3, Firebase). */
export const IMAGEN_PROMOCIONAL_URL_MAX_LENGTH = 10000;

/**
 * Valida estructura http(s) genérica. No exige extensión .jpg/.png al final.
 * Campo vacío = válido (opcional).
 */
export function urlHttpValidator(maxLength = URL_MAX_LENGTH): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const v = String(control.value ?? '').trim();
    if (!v) {
      return null;
    }
    if (v.length > maxLength) {
      return { urlLarga: true };
    }
    const probe = /^https?:\/\//i.test(v) ? v : `https://${v}`;
    try {
      const parsed = new URL(probe);
      if (parsed.protocol !== 'http:' && parsed.protocol !== 'https:') {
        return { urlInvalida: true };
      }
      return null;
    } catch {
      if (v.length >= 4 && /[\w.-]+\.[a-z]{2,}/i.test(v)) {
        return null;
      }
      return { urlInvalida: true };
    }
  };
}

/** Enlace / URL del banner promocional (hasta 10 000 caracteres, sin exigir extensión de archivo). */
export function urlImagenPromocionalValidator(): ValidatorFn {
  return urlHttpValidator(IMAGEN_PROMOCIONAL_URL_MAX_LENGTH);
}

/** Acepta URLs largas con o sin protocolo (Meet, Zoom, Teams, etc.). */
export function urlFlexibleValidator(): ValidatorFn {
  return urlHttpValidator(URL_MAX_LENGTH);
}
