import { Pipe, PipeTransform } from '@angular/core';

/** Muestra "Gratuito" cuando el precio es 0 o el evento está marcado como gratuito. */
@Pipe({
  name: 'precioEvento',
  standalone: true,
})
export class PrecioEventoPipe implements PipeTransform {
  transform(precio: number | null | undefined, gratuito?: boolean | null): string {
    if (gratuito || precio == null || precio === 0) {
      return 'Gratuito';
    }
    return `$${Number(precio).toLocaleString('es-CO', { maximumFractionDigits: 0 })}`;
  }
}

@Pipe({
  name: 'precioGratuito',
  standalone: true,
})
export class PrecioGratuitoPipe implements PipeTransform {
  transform(precio: number | null | undefined, gratuito?: boolean | null): boolean {
    return !!gratuito || precio == null || precio === 0;
  }
}
