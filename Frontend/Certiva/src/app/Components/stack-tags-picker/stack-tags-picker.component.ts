import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

export const STACK_TECNOLOGIAS_PREDEFINIDAS = [
  'Java',
  'Python',
  'JavaScript',
  'TypeScript',
  'Angular',
  'React',
  'Spring Boot',
  'Node.js',
  'Docker',
  'AWS',
  'PostgreSQL',
  '.NET',
  'Flutter',
];

@Component({
  selector: 'app-stack-tags-picker',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './stack-tags-picker.component.html',
  styleUrl: './stack-tags-picker.component.scss',
})
export class StackTagsPickerComponent {
  readonly opciones = STACK_TECNOLOGIAS_PREDEFINIDAS;

  @Input() seleccionados: string[] = [];
  @Output() seleccionadosChange = new EventEmitter<string[]>();

  toggle(tag: string): void {
    const set = new Set(this.seleccionados);
    if (set.has(tag)) {
      set.delete(tag);
    } else {
      set.add(tag);
    }
    this.seleccionados = [...set];
    this.seleccionadosChange.emit(this.seleccionados);
  }

  estaSeleccionado(tag: string): boolean {
    return this.seleccionados.includes(tag);
  }
}
