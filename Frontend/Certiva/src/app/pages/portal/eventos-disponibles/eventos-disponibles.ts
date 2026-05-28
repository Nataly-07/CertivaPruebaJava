import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { PrecioEventoPipe, PrecioGratuitoPipe } from '../../../pipes/precio-evento.pipe';
import { Router, RouterLink } from '@angular/router';
import { catchError, forkJoin, of } from 'rxjs';
import { EventoService } from '../../../Services/evento.service';
import { InscripcionService } from '../../../Services/inscripcion.service';
import { AuthService } from '../../../Services/auth.service';
import { EventoPublico } from '../../../Models/evento-publico';
import { TipoEventoEnum } from '../../../Models/evento-dto';
import {
  CHIPS_CATALOGO,
  FAVORITOS_STORAGE_KEY,
  FiltroCategoriaCatalogo,
  HERO_CATALOGO_IMAGEN,
  HERO_CATALOGO_IMAGEN_FALLBACKS,
} from '../../../constants/catalogo-assets';
import { etiquetaModalidad, etiquetaTipoEvento } from '../../../constants/ui-labels';
import { resolverUrlImagenEvento, placeholderGradienteTipo } from '../../../utils/evento-imagen.util';

@Component({
  selector: 'app-eventos-disponibles',
  standalone: true,
  imports: [CommonModule, RouterLink, DatePipe, PrecioEventoPipe, PrecioGratuitoPipe],
  templateUrl: './eventos-disponibles.html',
  styleUrl: './eventos-disponibles.scss',
})
export class EventosDisponibles implements OnInit {
  private eventoService = inject(EventoService);
  private inscripcionService = inject(InscripcionService);
  private router = inject(Router);
  readonly authService = inject(AuthService);

  readonly chips = CHIPS_CATALOGO;
  heroImage = HERO_CATALOGO_IMAGEN;
  private heroFallbackIndex = 0;
  readonly etiquetaTipo = etiquetaTipoEvento;
  readonly etiquetaMod = etiquetaModalidad;

  todosLosEventos: EventoPublico[] = [];
  eventosMostrados: EventoPublico[] = [];
  tipoSeleccionado: FiltroCategoriaCatalogo = 'Todos';
  terminoBusqueda = '';

  favoritos = new Set<number>();
  inscritosIds = new Set<number>();
  loading = true;
  errorMsg: string | null = null;

  ngOnInit(): void {
    this.cargarFavoritos();
    this.cargarCatalogo();
  }

  /** Prueba la siguiente extensión de Image_2 si el archivo no carga. */
  onHeroImageError(): void {
    const fallbacks = HERO_CATALOGO_IMAGEN_FALLBACKS;
    if (this.heroFallbackIndex >= fallbacks.length - 1) {
      return;
    }
    this.heroFallbackIndex += 1;
    this.heroImage = fallbacks[this.heroFallbackIndex];
  }

  /** Rol estudiante (en Certiva: ESTUDIANTE, no CLIENTE). */
  esEstudiante(): boolean {
    return this.authService.isEstudiante();
  }

  cargarCatalogo(): void {
    this.loading = true;
    this.errorMsg = null;

    const catalogo$ = this.eventoService.obtenerCatalogoPublico();
    const mis$ = this.authService.isLoggedIn()
      ? this.inscripcionService.listarMis().pipe(catchError(() => of([])))
      : of([]);

    forkJoin({ eventos: catalogo$, mis: mis$ }).subscribe({
      next: ({ eventos, mis }) => {
        this.todosLosEventos = eventos ?? [];
        this.inscritosIds = new Set(mis.map(m => m.idEvento));
        this.aplicarFiltrosCombinados();
        this.loading = false;
      },
      error: err => {
        this.loading = false;
        const msg = err?.error?.mensaje;
        this.errorMsg = msg
          ? `Error del servidor: ${msg}`
          : 'No se pudo cargar el catálogo de eventos. Verifique que el backend esté en ejecución.';
        console.error('[catalogo público]', err);
      },
    });
  }

  filtrarPorTipo(tipo: FiltroCategoriaCatalogo): void {
    this.tipoSeleccionado = tipo;
    this.aplicarFiltrosCombinados();
  }

  onBuscar(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.terminoBusqueda = (input.value ?? '').toLowerCase();
    this.aplicarFiltrosCombinados();
  }

  private aplicarFiltrosCombinados(): void {
    const chip = this.chips.find(c => c.id === this.tipoSeleccionado);
    const tipoFiltro = chip?.tipo;

    this.eventosMostrados = this.todosLosEventos.filter(evento => {
      const cumpleTipo = !tipoFiltro || evento.tipoEvento === tipoFiltro;
      const q = this.terminoBusqueda.trim();
      if (!q) {
        return cumpleTipo;
      }
      const desc = (evento.descripcion ?? '').toLowerCase();
      const cumpleTexto =
        evento.nombreEvento.toLowerCase().includes(q) ||
        desc.includes(q) ||
        evento.area.toLowerCase().includes(q) ||
        evento.tipoEvento.toLowerCase().includes(q);
      return cumpleTipo && cumpleTexto;
    });
  }

  calcularPorcentaje(inscritos: number, maximo: number | null | undefined): number {
    if (!maximo || maximo <= 0) {
      return 0;
    }
    return Math.min(100, Math.round((inscritos / maximo) * 100));
  }

  claseTagTipo(tipo: TipoEventoEnum): string {
    return `tag-${tipo.toLowerCase()}`;
  }

  claseBarraTipo(tipo: TipoEventoEnum): string {
    return `bar-fill--${tipo.toLowerCase()}`;
  }

  ubicacionTexto(evento: EventoPublico): string {
    if (evento.modalidad === 'VIRTUAL') {
      return evento.enlaceVirtual ? 'Plataforma virtual' : 'Virtual';
    }
    if (evento.modalidad === 'HIBRIDO') {
      return evento.ubicacion ? `${evento.ubicacion} (Híbrido)` : 'Híbrido';
    }
    return evento.ubicacion ?? 'Por confirmar';
  }

  instructorNombre(evento: EventoPublico): string {
    const n = evento.instructorNombres?.trim() ?? '';
    const a = evento.instructorApellidos?.trim() ?? '';
    const completo = `${n} ${a}`.trim();
    return completo || 'Por asignar';
  }

  instructorIniciales(evento: EventoPublico): string {
    const n = evento.instructorNombres?.[0] ?? 'C';
    const a = evento.instructorApellidos?.[0] ?? 'V';
    return `${n}${a}`.toUpperCase();
  }

  esFavorito(id: number): boolean {
    return this.favoritos.has(id);
  }

  toggleFavorito(evento: EventoPublico, ev: Event): void {
    ev.stopPropagation();
    if (this.favoritos.has(evento.idEvento)) {
      this.favoritos.delete(evento.idEvento);
    } else {
      this.favoritos.add(evento.idEvento);
    }
    localStorage.setItem(FAVORITOS_STORAGE_KEY, JSON.stringify([...this.favoritos]));
  }

  yaInscrito(id: number): boolean {
    return this.inscritosIds.has(id);
  }

  puedeInscribirse(evento: EventoPublico): boolean {
    return evento.hayCupoDisponible && !this.yaInscrito(evento.idEvento);
  }

  textoBotonInscripcion(evento: EventoPublico): string {
    if (this.yaInscrito(evento.idEvento)) {
      return 'Inscrito';
    }
    if (!evento.hayCupoDisponible) {
      return 'Agotado';
    }
    return '+ Inscribirse';
  }

  imagenEvento(evento: EventoPublico): string | null {
    return resolverUrlImagenEvento(evento.rutaImagenPromocional);
  }

  placeholderTipo(tipo: TipoEventoEnum): string {
    return placeholderGradienteTipo(tipo);
  }

  abrirDetalle(evento: EventoPublico): void {
    const base = this.authService.isLoggedIn() ? '/portal/eventos' : '/catalogo/eventos';
    this.router.navigate([base, evento.idEvento]);
  }

  inscribirse(evento: EventoPublico): void {
    if (!this.puedeInscribirse(evento)) {
      return;
    }
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: `/portal/eventos/${evento.idEvento}/inscribir` } });
      return;
    }
    this.router.navigate(['/portal/eventos', evento.idEvento, 'inscribir']);
  }

  private cargarFavoritos(): void {
    try {
      const raw = localStorage.getItem(FAVORITOS_STORAGE_KEY);
      if (raw) {
        this.favoritos = new Set(JSON.parse(raw) as number[]);
      }
    } catch {
      this.favoritos = new Set();
    }
  }
}
