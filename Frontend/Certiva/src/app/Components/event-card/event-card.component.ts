import { Component, Input, OnChanges, SimpleChanges, inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { EventoDTO, EventoCupoVerificacionDTO, UsuarioStaffDTO } from '../../Models/evento-dto';
import { PrecioEventoPipe, PrecioGratuitoPipe } from '../../pipes/precio-evento.pipe';
import { RolEtiquetaPipe } from '../../pipes/rol-etiqueta.pipe';
import { etiquetaModalidad, etiquetaTipoEvento } from '../../constants/ui-labels';
import { resolverUrlImagenEvento, placeholderGradienteTipo } from '../../utils/evento-imagen.util';

@Component({
  selector: 'app-event-card',
  standalone: true,
  imports: [CommonModule, RouterLink, DatePipe, PrecioEventoPipe, PrecioGratuitoPipe, RolEtiquetaPipe],
  templateUrl: './event-card.component.html',
  styleUrl: './event-card.component.scss',
})
export class EventCardComponent implements OnChanges {
  private router = inject(Router);

  @Input({ required: true }) evento!: EventoDTO;
  @Input() cupo: EventoCupoVerificacionDTO | null = null;
  @Input() yaInscrito = false;

  etiquetaTipo(tipo: string): string {
    return etiquetaTipoEvento(tipo);
  }

  etiquetaMod(modalidad: string): string {
    return etiquetaModalidad(modalidad);
  }

  tipoClase(tipo: string): string {
    const map: Record<string, string> = {
      CURSO: 'tipo--curso',
      HACKATHON: 'tipo--hackathon',
      TALLER: 'tipo--taller',
      FERIA: 'tipo--feria',
    };
    return map[tipo] ?? 'tipo--curso';
  }

  modalidadIcono(modalidad: string): string {
    const map: Record<string, string> = {
      PRESENCIAL: 'bi-geo-alt',
      VIRTUAL: 'bi-camera-video',
      HIBRIDO: 'bi-wifi',
    };
    return map[modalidad] ?? 'bi-calendar-event';
  }

  imagenFallida = false;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['evento']) {
      this.imagenFallida = false;
    }
  }

  imagenUrl(): string | null {
    if (this.imagenFallida) {
      return null;
    }
    return resolverUrlImagenEvento(
      this.evento.rutaImagenPromocional ?? this.evento.imagenPromocionalUrl,
    );
  }

  onImagenError(): void {
    this.imagenFallida = true;
  }

  placeholderGradient(): string {
    return placeholderGradienteTipo(this.evento.tipoEvento);
  }

  staffPrincipal(): UsuarioStaffDTO | null {
    const prof = this.evento.profesoresColaboradores?.[0];
    if (prof) {
      return prof;
    }
    return this.evento.monitoresAsignados?.[0] ?? null;
  }

  iniciales(staff: UsuarioStaffDTO): string {
    const n = (staff.nombres?.[0] ?? '').toUpperCase();
    const a = (staff.apellidos?.[0] ?? '').toUpperCase();
    return `${n}${a}` || '?';
  }

  avatarColor(staff: UsuarioStaffDTO): string {
    const palette = ['#3b82f6', '#8b5cf6', '#14b8a6', '#f59e0b', '#ec4899'];
    const idx = (staff.idUsuario ?? 0) % palette.length;
    return palette[idx];
  }

  inscritosActivos(): number {
    return this.cupo?.inscritosActivos ?? 0;
  }

  aforoMaximo(): number {
    return this.cupo?.aforoMaximo ?? this.evento.aforoMaximo ?? 0;
  }

  porcentajeCupo(): number {
    const max = this.aforoMaximo();
    if (!max) {
      return 0;
    }
    return Math.min(100, Math.round((this.inscritosActivos() / max) * 100));
  }

  cupoTexto(): string {
    const max = this.aforoMaximo();
    if (!max) {
      return 'Cupos ilimitados';
    }
    return `${this.inscritosActivos()}/${max} Cupos`;
  }

  barraCupoClase(): string {
    const p = this.porcentajeCupo();
    if (p >= 90) {
      return 'cupo-fill--critico';
    }
    if (p >= 70) {
      return 'cupo-fill--alto';
    }
    return '';
  }

  ultimosLugares(): boolean {
    const p = this.porcentajeCupo();
    return p >= 85 && !this.yaInscrito && (this.cupo?.hayCupoDisponible ?? true);
  }

  sinCupo(): boolean {
    return this.cupo != null && !this.cupo.hayCupoDisponible;
  }

  ubicacionTexto(): string {
    if (this.evento.modalidad === 'VIRTUAL') {
      return this.evento.enlaceVirtual ? 'Plataforma virtual' : 'Virtual';
    }
    if (this.evento.modalidad === 'HIBRIDO') {
      return this.evento.ubicacion ?? 'Presencial + Virtual';
    }
    return this.evento.ubicacion ?? 'Por confirmar';
  }

  verDetalle(): void {
    this.router.navigate(['/portal/eventos', this.evento.idEvento]);
  }

  horaInicio(): string {
    if (!this.evento.fechaInicio) {
      return '';
    }
    return new Date(this.evento.fechaInicio).toLocaleTimeString('es-CO', {
      hour: 'numeric',
      minute: '2-digit',
      hour12: true,
    });
  }
}
