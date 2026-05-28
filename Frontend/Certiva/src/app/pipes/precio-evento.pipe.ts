import { Pipe, PipeTransform } from '@angular/core';
import { formatearPrecioCop } from '../utils/currency.util';

/** Muestra precio en pesos colombianos (COP) o "Gratuito". */
@Pipe({
  name: 'precioEvento',
  standalone: true,
})
export class PrecioEventoPipe implements PipeTransform {
  transform(precio: number | null | undefined, gratuito?: boolean | null): string {
    return formatearPrecioCop(precio, gratuito);
  }
}

@Pipe({
  name: 'precioGratuito',
  standalone: true,
})
export class PrecioGratuitoPipe implements PipeTransform {
  transform(precio: number | null | undefined, gratuito?: boolean | null): boolean {
    return formatearPrecioCop(precio, gratuito) === 'Gratuito';
  }
}
