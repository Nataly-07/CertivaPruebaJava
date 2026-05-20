import { Component, Input } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { EventoDTO, EventoCupoVerificacionDTO, UsuarioStaffDTO } from '../../Models/evento-dto';
import { PrecioEventoPipe, PrecioGratuitoPipe } from '../../pipes/precio-evento.pipe';
import { RolEtiquetaPipe } from '../../pipes/rol-etiqueta.pipe';
import { etiquetaModalidad, etiquetaTipoEvento } from '../../constants/ui-labels';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-event-card',
  standalone: true,
  imports: [CommonModule, RouterLink, DatePipe, PrecioEventoPipe, PrecioGratuitoPipe, RolEtiquetaPipe],
  templateUrl: './event-card.component.html',
  styleUrl: './event-card.component.scss',
})
export class EventCardComponent {
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

  imagenUrl(): string | null {
    const ruta = this.evento.rutaImagenPromocional;
    if (!ruta?.trim()) {
      return null;
    }
    if (ruta.startsWith('http://') || ruta.startsWith('https://')) {
      return ruta;
    }
    const apiBase = environment.API_URL.replace(/\/api\/?$/, '');
    return `${apiBase}${ruta.startsWith('/') ? ruta : `/${ruta}`}`;
  }

  placeholderGradient(): string {
    const gradients: Record<string, string> = {
      CURSO: 'linear-gradient(135deg, #1e3a5f 0%, #3b82f6 100%)',
      HACKATHON: 'linear-gradient(135deg, #4c1d95 0%, #7c3aed 100%)',
      TALLER: 'linear-gradient(135deg, #134e4a 0%, #14b8a6 100%)',
      FERIA: 'linear-gradient(135deg, #78350f 0%, #f59e0b 100%)',
    };
    return gradients[this.evento.tipoEvento] ?? gradients['CURSO'];
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
