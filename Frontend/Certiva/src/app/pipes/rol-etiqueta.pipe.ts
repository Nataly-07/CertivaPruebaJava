import { Pipe, PipeTransform } from '@angular/core';
import { etiquetaRol } from '../constants/ui-labels';

@Pipe({
  name: 'rolEtiqueta',
  standalone: true,
})
export class RolEtiquetaPipe implements PipeTransform {
  transform(codigoRol: string | null | undefined): string {
    return etiquetaRol(codigoRol);
  }
}
