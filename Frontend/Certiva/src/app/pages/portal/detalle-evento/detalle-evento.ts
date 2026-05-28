import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EventoService } from '../../../Services/evento.service';
import { AuthService } from '../../../Services/auth.service';
import { InscripcionService } from '../../../Services/inscripcion.service';
import { EventoPublicoDTO } from '../../../Models/evento-dto';
import { PrecioEventoPipe, PrecioGratuitoPipe } from '../../../pipes/precio-evento.pipe';
import { etiquetaModalidad, etiquetaTipoEvento } from '../../../constants/ui-labels';
import { resolverUrlImagenEvento, placeholderGradienteTipo } from '../../../utils/evento-imagen.util';
import { catchError, forkJoin, of } from 'rxjs';

@Component({
  selector: 'app-detalle-evento',
  standalone: true,
  imports: [CommonModule, RouterLink, DatePipe, PrecioEventoPipe, PrecioGratuitoPipe],
  templateUrl: './detalle-evento.html',
  styleUrl: './detalle-evento.scss',
})
export class DetalleEvento implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private eventoService = inject(EventoService);
  private inscripcionService = inject(InscripcionService);
  auth = inject(AuthService);

  evento = signal<EventoPublicoDTO | null>(null);
  loading = signal(true);
  errorMsg = signal<string | null>(null);
  imagenFallida = signal(false);
  idsInscritos = signal<Set<number>>(new Set());

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isFinite(id)) {
      this.loading.set(false);
      this.errorMsg.set('Evento no válido.');
      return;
    }
    const mis$ = this.auth.isLoggedIn()
      ? this.inscripcionService.listarMis().pipe(catchError(() => of([])))
      : of([]);
    forkJoin({
      evento: this.eventoService.obtenerPublicoPorId(id),
      mis: mis$,
    }).subscribe({
      next: ({ evento, mis }) => {
        this.evento.set(evento);
        this.idsInscritos.set(new Set(mis.map(m => m.idEvento)));
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.errorMsg.set('No se encontró el evento o ya no está disponible.');
      },
    });
  }

  imagenUrl(ev: EventoPublicoDTO): string | null {
    if (this.imagenFallida()) {
      return null;
    }
    return resolverUrlImagenEvento(ev.rutaImagenPromocional);
  }

  placeholder(ev: EventoPublicoDTO): string {
    return placeholderGradienteTipo(ev.tipoEvento);
  }

  onImagenError(): void {
    this.imagenFallida.set(true);
  }

  etiquetaTipo(tipo: string): string {
    return etiquetaTipoEvento(tipo);
  }

  etiquetaMod(modalidad: string): string {
    return etiquetaModalidad(modalidad);
  }

  instructorNombre(ev: EventoPublicoDTO): string {
    const n = `${ev.instructorNombres ?? ''} ${ev.instructorApellidos ?? ''}`.trim();
    return n || 'Por confirmar';
  }

  instructorIniciales(ev: EventoPublicoDTO): string {
    const n = (ev.instructorNombres?.[0] ?? '').toUpperCase();
    const a = (ev.instructorApellidos?.[0] ?? '').toUpperCase();
    return `${n}${a}` || '?';
  }

  ubicacionTexto(ev: EventoPublicoDTO): string {
    if (ev.modalidad === 'VIRTUAL') {
      return 'Sesión virtual';
    }
    if (ev.modalidad === 'HIBRIDO') {
      return ev.ubicacion ? `${ev.ubicacion} (híbrido)` : 'Presencial + virtual';
    }
    return ev.ubicacion ?? 'Por confirmar';
  }

  porcentajeCupo(ev: EventoPublicoDTO): number {
    const max = ev.aforoMaximo ?? 0;
    const ins = ev.inscritosActivos ?? 0;
    if (!max) {
      return 0;
    }
    return Math.min(100, Math.round((ins / max) * 100));
  }

  yaInscrito(id: number): boolean {
    return this.idsInscritos().has(id);
  }

  rutaVolver(): string {
    return this.auth.isLoggedIn() ? '/portal/eventos' : '/catalogo';
  }

  inscribirse(ev: EventoPublicoDTO): void {
    if (!this.auth.isLoggedIn()) {
      this.router.navigate(['/login'], {
        queryParams: { returnUrl: `/portal/eventos/${ev.idEvento}/inscribir` },
      });
      return;
    }
    this.router.navigate(['/portal/eventos', ev.idEvento, 'inscribir']);
  }
}
