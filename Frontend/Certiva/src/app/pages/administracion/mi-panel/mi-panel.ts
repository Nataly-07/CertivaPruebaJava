import { Component, OnInit, inject, signal } from '@angular/core';
import { Observable } from 'rxjs';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../Services/auth.service';
import { EventoService } from '../../../Services/evento.service';
import {
  EventoAsistenciaEnVivoDTO,
  EventoRevisionAlumnoDTO,
  EventoRevisionPanelDTO,
  GuardarRevisionAlumnoDTO,
  ProfesorEventoTarjetaDTO,
  ProfesorPanelDTO,
} from '../../../Models/portal-dto';
import { etiquetaTipoEvento } from '../../../constants/ui-labels';
import { etiquetaEstadoEvento } from '../../../constants/estado-evento';
import { EventoCierreResultadoDTO } from '../../../Models/evento-dto';
import { mensajeErrorHttp } from '../../../utils/http-error-message';
import { ProfesorSidebarComponent } from '../../../Components/profesor-sidebar/profesor-sidebar.component';

type TabPanel = 'EN_CURSO' | 'EN_REVISION' | 'HISTORIAL';

@Component({
  selector: 'app-mi-panel',
  standalone: true,
  imports: [CommonModule, DatePipe, FormsModule, ProfesorSidebarComponent],
  templateUrl: './mi-panel.html',
  styleUrl: './mi-panel.scss',
})
export class MiPanel implements OnInit {
  authService = inject(AuthService);
  private eventoService = inject(EventoService);
  private router = inject(Router);

  panel = signal<ProfesorPanelDTO | null>(null);
  loading = signal(true);
  errorMsg = signal<string | null>(null);
  accionMsg = signal<string | null>(null);
  sidebarCollapsed = false;
  accionandoId = signal<number | null>(null);

  tabActiva = signal<TabPanel>('EN_CURSO');
  modalAsistenciaAbierto = signal(false);
  asistenciaCargando = signal(false);
  asistenciaEnVivo = signal<EventoAsistenciaEnVivoDTO | null>(null);
  eventoAsistenciaSeleccionado = signal<ProfesorEventoTarjetaDTO | null>(null);

  modalRevisionAbierto = signal(false);
  revisionCargando = signal(false);
  revision = signal<EventoRevisionPanelDTO | null>(null);
  eventoRevisionSeleccionado = signal<ProfesorEventoTarjetaDTO | null>(null);
  filasRevisionEdit: EventoRevisionAlumnoDTO[] = [];
  guardandoEvaluaciones = signal(false);
  pasoCertificacion = signal(false);

  ngOnInit(): void {
    this.recargarPanel();
  }

  cambiarTab(tab: TabPanel): void {
    this.tabActiva.set(tab);
  }

  eventosTab(): ProfesorEventoTarjetaDTO[] {
    const p = this.panel();
    if (!p) return [];
    switch (this.tabActiva()) {
      case 'EN_CURSO':
        return p.enCurso ?? [];
      case 'EN_REVISION':
        return p.pendientesCierre ?? [];
      case 'HISTORIAL':
        return p.historial ?? [];
      default:
        return [];
    }
  }

  contarTab(tab: TabPanel): number {
    const p = this.panel();
    if (!p) return 0;
    if (tab === 'EN_CURSO') return p.enCurso?.length ?? 0;
    if (tab === 'EN_REVISION') return p.pendientesCierre?.length ?? 0;
    return p.historial?.length ?? 0;
  }

  etiquetaTipo(tipo?: string): string {
    return etiquetaTipoEvento(tipo);
  }

  etiquetaEstado(codigo?: string): string {
    return etiquetaEstadoEvento(codigo);
  }

  nombreMonitor(ev: ProfesorEventoTarjetaDTO): string {
    const n = (ev.monitorNombre ?? '').trim();
    const a = (ev.monitorApellidos ?? '').trim();
    return `${n} ${a}`.trim() || 'Sin asignar';
  }

  horarioRango(inicio?: string, fin?: string): string {
    if (!inicio || !fin) return '—';
    const fmt = (iso: string) =>
      new Date(iso).toLocaleTimeString('es-CO', { hour: '2-digit', minute: '2-digit' });
    return `${fmt(inicio)} – ${fmt(fin)}`;
  }

  irCrearEvento(): void {
    this.router.navigate(['/admin/eventos']);
  }

  abrirAsistenciaPorId(idEvento: number, nombreEvento?: string): void {
    this.abrirAsistenciaEnVivo({
      idEvento,
      nombreEvento: nombreEvento ?? 'Evento',
    } as ProfesorEventoTarjetaDTO);
  }

  abrirAsistenciaEnVivo(ev: ProfesorEventoTarjetaDTO): void {
    this.eventoAsistenciaSeleccionado.set(ev);
    this.modalAsistenciaAbierto.set(true);
    this.asistenciaCargando.set(true);
    this.asistenciaEnVivo.set(null);
    this.eventoService.obtenerAsistenciaEnVivo(ev.idEvento).subscribe({
      next: data => {
        this.asistenciaEnVivo.set(data);
        this.asistenciaCargando.set(false);
      },
      error: err => {
        this.asistenciaCargando.set(false);
        this.modalAsistenciaAbierto.set(false);
        this.accionMsg.set(err?.error?.mensaje || 'No se pudo cargar la asistencia.');
      },
    });
  }

  cerrarModalAsistencia(): void {
    this.modalAsistenciaAbierto.set(false);
    this.asistenciaEnVivo.set(null);
    this.eventoAsistenciaSeleccionado.set(null);
  }

  refrescarAsistenciaEnVivo(): void {
    const ev = this.eventoAsistenciaSeleccionado();
    if (!ev) return;
    this.asistenciaCargando.set(true);
    this.eventoService.obtenerAsistenciaEnVivo(ev.idEvento).subscribe({
      next: data => {
        this.asistenciaEnVivo.set(data);
        this.asistenciaCargando.set(false);
      },
      error: () => this.asistenciaCargando.set(false),
    });
  }

  iniciarRevision(ev: ProfesorEventoTarjetaDTO): void {
    this.ejecutarAccion(ev.idEvento, () => this.eventoService.iniciarRevision(ev.idEvento), 'Revisión académica iniciada.');
  }

  abrirRevisionCierre(ev: ProfesorEventoTarjetaDTO): void {
    if (ev.requiereIniciarRevision) {
      if (!confirm('Debe iniciar la revisión académica antes de evaluar alumnos. ¿Iniciar ahora?')) {
        return;
      }
      this.eventoService.iniciarRevision(ev.idEvento).subscribe({
        next: () => this.cargarModalRevision(ev),
        error: err => this.accionMsg.set(err?.error?.mensaje || 'No se pudo iniciar la revisión.'),
      });
      return;
    }
    this.cargarModalRevision(ev);
  }

  private cargarModalRevision(ev: ProfesorEventoTarjetaDTO): void {
    this.eventoRevisionSeleccionado.set(ev);
    this.modalRevisionAbierto.set(true);
    this.pasoCertificacion.set(false);
    this.revisionCargando.set(true);
    this.revision.set(null);
    this.eventoService.obtenerRevisionCierre(ev.idEvento).subscribe({
      next: data => {
        this.revision.set(data);
        this.filasRevisionEdit = data.alumnos.map(a => ({ ...a }));
        this.revisionCargando.set(false);
      },
      error: err => {
        this.revisionCargando.set(false);
        this.modalRevisionAbierto.set(false);
        this.accionMsg.set(err?.error?.mensaje || 'No se pudo cargar la revisión.');
      },
    });
  }

  cerrarModalRevision(): void {
    this.modalRevisionAbierto.set(false);
    this.revision.set(null);
    this.eventoRevisionSeleccionado.set(null);
    this.pasoCertificacion.set(false);
  }

  guardarEvaluaciones(): void {
    const ev = this.eventoRevisionSeleccionado();
    if (!ev) return;
    const payload: GuardarRevisionAlumnoDTO[] = this.filasRevisionEdit.map(f => ({
      idInscripcion: f.idInscripcion,
      nota: f.nota ?? null,
      observaciones: f.observaciones ?? null,
    }));
    this.guardandoEvaluaciones.set(true);
    this.eventoService.guardarEvaluacionesRevision(ev.idEvento, payload).subscribe({
      next: data => {
        this.revision.set(data);
        this.filasRevisionEdit = data.alumnos.map(a => ({ ...a }));
        this.guardandoEvaluaciones.set(false);
        this.accionMsg.set('Evaluaciones guardadas correctamente.');
      },
      error: err => {
        this.guardandoEvaluaciones.set(false);
        this.accionMsg.set(err?.error?.mensaje || 'No se pudieron guardar las evaluaciones.');
      },
    });
  }

  irPasoCertificacion(): void {
    this.pasoCertificacion.set(true);
  }

  volverPasoEvaluacion(): void {
    this.pasoCertificacion.set(false);
  }

  contarElegibles(): number {
    return this.revision()?.elegiblesCertificado ?? this.filasRevisionEdit.filter(a => a.elegibleCertificado).length;
  }

  confirmarCierreDesdeModal(): void {
    const ev = this.eventoRevisionSeleccionado();
    if (!ev) return;
    if (
      !confirm(
        `¿Disparar la generación masiva de certificados PDF para ${this.contarElegibles()} alumno(s) elegibles? El evento quedará cerrado.`
      )
    ) {
      return;
    }
    this.accionandoId.set(ev.idEvento);
    this.eventoService.cerrarYCertificar(ev.idEvento).subscribe({
      next: (res: EventoCierreResultadoDTO) => {
        this.accionandoId.set(null);
        this.accionMsg.set(res.mensaje);
        this.cerrarModalRevision();
        this.recargarPanel();
      },
      error: err => {
        this.accionandoId.set(null);
        this.accionMsg.set(err?.error?.mensaje || 'No se pudo cerrar el evento.');
      },
    });
  }

  private ejecutarAccion(id: number, op: () => Observable<string>, ok: string): void {
    this.accionandoId.set(id);
    op().subscribe({
      next: () => {
        this.accionandoId.set(null);
        this.accionMsg.set(ok);
        this.recargarPanel();
      },
      error: err => {
        this.accionandoId.set(null);
        this.accionMsg.set(err?.error?.mensaje || 'Operación no permitida.');
      },
    });
  }

  private recargarPanel(): void {
    this.loading.set(true);
    this.eventoService.obtenerPanelProfesor().subscribe({
      next: data => {
        this.panel.set(data);
        this.loading.set(false);
        this.seleccionarTabConDatos(data);
      },
      error: err => {
        this.loading.set(false);
        this.errorMsg.set(mensajeErrorHttp(err, 'No se pudo cargar el panel.'));
      },
    });
  }

  private seleccionarTabConDatos(p: ProfesorPanelDTO): void {
    if ((p.enCurso?.length ?? 0) > 0) {
      this.tabActiva.set('EN_CURSO');
    } else if ((p.pendientesCierre?.length ?? 0) > 0) {
      this.tabActiva.set('EN_REVISION');
    } else if ((p.historial?.length ?? 0) > 0) {
      this.tabActiva.set('HISTORIAL');
    }
  }
}
