import { Component, Input, Output, EventEmitter, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, debounceTime, distinctUntilChanged, switchMap, of } from 'rxjs';
import { UsuarioService } from '../../Services/usuario.service';
import { UsuarioStaffDTO } from '../../Models/evento-dto';
import { STAFF_UI, TipoStaffUi } from '../../constants/ui-labels';

@Component({
  selector: 'app-staff-autocomplete',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './staff-autocomplete.component.html',
  styleUrl: './staff-autocomplete.component.scss',
})
export class StaffAutocompleteComponent implements OnInit {
  /** Tipo de personal a buscar (valor interno; no se muestra al usuario). */
  @Input({ required: true }) tipoStaff!: TipoStaffUi;
  @Input() seleccionados: UsuarioStaffDTO[] = [];
  @Output() seleccionadosChange = new EventEmitter<UsuarioStaffDTO[]>();

  private usuarioService = inject(UsuarioService);
  private busqueda$ = new Subject<string>();

  termino = '';
  sugerencias: UsuarioStaffDTO[] = [];
  cargando = false;

  get titulo(): string {
    return STAFF_UI[this.tipoStaff].titulo;
  }

  get placeholder(): string {
    return STAFF_UI[this.tipoStaff].placeholder;
  }

  get hint(): string {
    return STAFF_UI[this.tipoStaff].hint;
  }

  private get codigoApi(): 'PROFESOR' | 'MONITOR' {
    return this.tipoStaff === 'profesor' ? 'PROFESOR' : 'MONITOR';
  }

  ngOnInit(): void {
    this.busqueda$
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap(q => {
          this.cargando = true;
          const query = q?.trim() ?? '';
          if (query.length < 2) {
            this.cargando = false;
            return of([]);
          }
          return this.usuarioService.buscarStaffPorRol(this.codigoApi, query);
        })
      )
      .subscribe({
        next: lista => {
          const ids = new Set(this.seleccionados.map(s => s.idUsuario));
          this.sugerencias = lista.filter(u => !ids.has(u.idUsuario));
          this.cargando = false;
        },
        error: () => {
          this.sugerencias = [];
          this.cargando = false;
        },
      });
  }

  onBuscar(): void {
    this.busqueda$.next(this.termino);
  }

  agregar(u: UsuarioStaffDTO): void {
    if (this.seleccionados.some(s => s.idUsuario === u.idUsuario)) {
      return;
    }
    const next = [...this.seleccionados, u];
    this.seleccionadosChange.emit(next);
    this.seleccionados = next;
    this.termino = '';
    this.sugerencias = [];
  }

  quitar(id: number): void {
    const next = this.seleccionados.filter(s => s.idUsuario !== id);
    this.seleccionadosChange.emit(next);
    this.seleccionados = next;
  }
}
