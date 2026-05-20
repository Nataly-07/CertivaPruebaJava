import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export const URL_MAX_LENGTH = 2048;

/** Acepta URLs largas con o sin protocolo (Meet, Zoom, Teams, etc.). */
export function urlFlexibleValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const v = String(control.value ?? '').trim();
    if (!v) {
      return null;
    }
    if (v.length > URL_MAX_LENGTH) {
      return { urlLarga: true };
    }
    const probe = /^https?:\/\//i.test(v) ? v : `https://${v}`;
    try {
      new URL(probe);
      return null;
    } catch {
      if (v.length >= 4 && /[\w.-]+\.[a-z]{2,}/i.test(v)) {
        return null;
      }
      return { urlInvalida: true };
    }
  };
}
