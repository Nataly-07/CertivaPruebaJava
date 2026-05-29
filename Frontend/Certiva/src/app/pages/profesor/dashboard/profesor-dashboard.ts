import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../Services/auth.service';
import { EventoService } from '../../../Services/evento.service';
import {
  EventoAsistenciaEnVivoDTO,
  ProfesorAlumnoAsistenciaDTO,
  ProfesorEventoTarjetaDTO,
  ProfesorPanelDTO,
} from '../../../Models/portal-dto';
import { EventoCierreResultadoDTO } from '../../../Models/evento-dto';
import { mensajeErrorHttp } from '../../../utils/http-error-message';
import { ProfesorSidebarComponent } from '../../../Components/profesor-sidebar/profesor-sidebar.component';
import { etiquetaEstadoProfesor, etiquetaTipoEvento } from '../../../constants/profesor-ui';

type VistaProfesor = 'dashboard' | 'cursos' | 'historial';

@Component({
  selector: 'app-profesor-dashboard',
  standalone: true,
  imports: [CommonModule, DatePipe, ProfesorSidebarComponent],
  templateUrl: './profesor-dashboard.html',
  styleUrl: './profesor-dashboard.scss',
})
export class ProfesorDashboard implements OnInit {
  private authService = inject(AuthService);
  private eventoService = inject(EventoService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  panel = signal<ProfesorPanelDTO | null>(null);
  loading = signal(true);
  errorMsg = signal<string | null>(null);
  accionMsg = signal<string | null>(null);
  sidebarCollapsed = false;
  accionandoId = signal<number | null>(null);

  vista = signal<VistaProfesor>('dashboard');

  drawerAbierto = signal(false);
  drawerCargando = signal(false);
  matriz = signal<EventoAsistenciaEnVivoDTO | null>(null);
  cursoSeleccionado = signal<ProfesorEventoTarjetaDTO | null>(null);
  justificandoId = signal<number | null>(null);

  ngOnInit(): void {
    this.route.data.subscribe((data) => {
      this.vista.set((data['vista'] as VistaProfesor) ?? 'dashboard');
    });
    this.recargarPanel();
  }

  get usuario() {
    return this.authService.getUsuario();
  }

  nombreProfesor(): string {
    const u = this.usuario;
    if (!u) return 'Profesor';
    return `${u.nombres} ${u.apellidos}`.trim();
  }

  recargarPanel(): void {
    this.loading.set(true);
    this.errorMsg.set(null);
    const usuarioLogueado = this.authService.getUsuario();
    this.eventoService.obtenerPanelProfesor().subscribe({
      next: (data) => {
        this.panel.set(data);
        this.loading.set(false);
        if (!data.totalEventos) {
          console.debug('[profesor-panel] Sin cursos asignados', {
            idUsuarioSesion: usuarioLogueado?.idUsuario,
            correo: usuarioLogueado?.correo,
            endpoint: 'GET /api/eventos/mi-panel',
            nota: 'El backend busca eventos donde id_creador o evento_profesores coinciden con este idUsuario.',
          });
        }
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMsg.set(mensajeErrorHttp(err, 'No se pudo cargar el panel del profesor.'));
      },
    });
  }

  cursosVisibles(): ProfesorEventoTarjetaDTO[] {
    const p = this.panel();
    if (!p) return [];
    const v = this.vista();
    if (v === 'historial') return p.historial ?? [];
    if (v === 'cursos') return [...(p.enCurso ?? []), ...(p.pendientesCierre ?? [])];
    return [...(p.enCurso ?? []), ...(p.pendientesCierre ?? [])];
  }

  tituloSeccion(): string {
    switch (this.vista()) {
      case 'historial':
        return 'Historial de cierre';
      case 'cursos':
        return 'Mis cursos asignados';
      default:
        return 'Mis cursos asignados';
    }
  }

  etiquetaTipo = etiquetaTipoEvento;
  etiquetaEstado = etiquetaEstadoProfesor;

  textoProgreso(ev: ProfesorEventoTarjetaDTO): string {
    const y = ev.sesionesTotales ?? 1;
    const x = ev.sesionActual ?? 0;
    return `Clase ${x} de ${y}`;
  }

  claseBadge(ev: ProfesorEventoTarjetaDTO): string {
    const op = ev.estadoOperativo ?? '';
    if (op === 'PROXIMO') return 'badge-inscripciones';
    if (op === 'EN_CURSO') return 'badge-en-curso';
    if (op === 'EN_REVISION') return 'badge-por-certificar';
    if (op === 'FINALIZADO_POR_TIEMPO') return 'badge-por-certificar';
    if (op === 'CERRADO_Y_CERTIFICADO') return 'badge-cerrado';
    return 'badge-default';
  }

  puedeClausurar(ev: ProfesorEventoTarjetaDTO): boolean {
    return !!ev.listoParaClausurar && ev.estadoOperativo === 'EN_REVISION';
  }

  clausurarDeshabilitado(ev: ProfesorEventoTarjetaDTO): boolean {
    const op = ev.estadoOperativo;
    return op === 'PROXIMO' || op === 'EN_CURSO' || op === 'CERRADO_Y_CERTIFICADO' || op === 'EVENT_CANCELLED';
  }

  abrirMatriz(ev: ProfesorEventoTarjetaDTO): void {
    if (ev.requiereIniciarRevision) {
      if (!confirm('Debe iniciar la revisión académica antes de auditar el acta. ¿Iniciar ahora?')) {
        return;
      }
      this.accionandoId.set(ev.idEvento);
      this.eventoService.iniciarRevision(ev.idEvento).subscribe({
        next: () => {
          this.accionandoId.set(null);
          this.accionMsg.set('Revisión académica iniciada.');
          this.recargarPanel();
          this.cargarMatriz({ ...ev, requiereIniciarRevision: false, estadoOperativo: 'EN_REVISION' });
        },
        error: (err) => {
          this.accionandoId.set(null);
          this.accionMsg.set(err?.error?.mensaje || 'No se pudo iniciar la revisión.');
        },
      });
      return;
    }
    this.cargarMatriz(ev);
  }

  private cargarMatriz(ev: ProfesorEventoTarjetaDTO): void {
    this.cursoSeleccionado.set(ev);
    this.drawerAbierto.set(true);
    this.drawerCargando.set(true);
    this.matriz.set(null);
    this.eventoService.obtenerAsistenciaEnVivo(ev.idEvento).subscribe({
      next: (data) => {
        this.matriz.set(data);
        this.drawerCargando.set(false);
      },
      error: (err) => {
        this.drawerCargando.set(false);
        this.drawerAbierto.set(false);
        this.accionMsg.set(err?.error?.mensaje || 'No se pudo cargar la matriz de asistencia.');
      },
    });
  }

  cerrarDrawer(): void {
    this.drawerAbierto.set(false);
    this.matriz.set(null);
    this.cursoSeleccionado.set(null);
  }

  refrescarMatriz(): void {
    const ev = this.cursoSeleccionado();
    if (!ev) return;
    this.drawerCargando.set(true);
    this.eventoService.obtenerAsistenciaEnVivo(ev.idEvento).subscribe({
      next: (data) => {
        this.matriz.set(data);
        this.drawerCargando.set(false);
      },
      error: () => this.drawerCargando.set(false),
    });
  }

  umbralAsistencia(): number {
    return this.matriz()?.porcentajeAsistenciaMinimo ?? 80;
  }

  esElegible(al: ProfesorAlumnoAsistenciaDTO): boolean {
    return al.porcentajeAsistencia >= this.umbralAsistencia();
  }

  justificarAsistencia(al: ProfesorAlumnoAsistenciaDTO): void {
    const ev = this.cursoSeleccionado();
    if (!ev) return;
    const justification = prompt(
      'Justificación obligatoria para registrar asistencia manual (fallo técnico del dispositivo del alumno):'
    );
    if (!justification?.trim()) return;
    this.justificandoId.set(al.idInscripcion);
    this.eventoService
      .registrarAsistenciaManual({
        eventId: ev.idEvento,
        idInscripcion: al.idInscripcion,
        justification: justification.trim(),
      })
      .subscribe({
        next: (res) => {
          this.justificandoId.set(null);
          this.accionMsg.set(res.mensaje || 'Asistencia justificada correctamente.');
          this.refrescarMatriz();
        },
        error: (err) => {
          this.justificandoId.set(null);
          this.accionMsg.set(err?.error?.mensaje || 'No se pudo justificar la asistencia.');
        },
      });
  }

  confirmarClausura(): void {
    const ev = this.cursoSeleccionado();
    const m = this.matriz();
    if (!ev || !m?.listoParaClausurar) {
      this.accionMsg.set('La clausura solo está disponible cuando el curso está POR CERTIFICAR y las sesiones están completas.');
      return;
    }
    if (
      !confirm(
        '¿Está seguro de clausurar el curso? Esta acción es inmutable y congelará los listados de asistencia para siempre.'
      )
    ) {
      return;
    }
    this.accionandoId.set(ev.idEvento);
    this.eventoService.clausurar(ev.idEvento).subscribe({
      next: (res: EventoCierreResultadoDTO) => {
        this.accionandoId.set(null);
        this.accionMsg.set(res.mensaje || 'Curso clausurado y certificados generados.');
        this.cerrarDrawer();
        this.recargarPanel();
        void this.router.navigate(['/profesor/dashboard']);
      },
      error: (err) => {
        this.accionandoId.set(null);
        this.accionMsg.set(err?.error?.mensaje || 'No se pudo clausurar el curso.');
      },
    });
  }

  clausurarDesdeFila(ev: ProfesorEventoTarjetaDTO): void {
    this.abrirMatriz(ev);
  }

  irProponerEvento(): void {
    void this.router.navigate(['/admin/eventos'], { queryParams: { crear: '1' } });
  }
}
