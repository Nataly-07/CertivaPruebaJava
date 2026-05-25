import { Component, OnInit, inject, signal } from '@angular/core';
import { Observable } from 'rxjs';
import { CommonModule, DatePipe } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../Services/auth.service';
import { EventoService } from '../../../Services/evento.service';
import {
  EventoRevisionPanelDTO,
  ProfesorEventoTarjetaDTO,
  ProfesorPanelDTO,
} from '../../../Models/portal-dto';
import { etiquetaTipoEvento } from '../../../constants/ui-labels';
import { etiquetaEstadoEvento } from '../../../constants/estado-evento';
import { EventoCierreResultadoDTO } from '../../../Models/evento-dto';
import { mensajeErrorHttp } from '../../../utils/http-error-message';

type TabPanel = 'EN_CURSO' | 'PENDIENTES' | 'HISTORIAL';

@Component({
  selector: 'app-mi-panel',
  standalone: true,
  imports: [CommonModule, DatePipe, RouterLink, RouterLinkActive],
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
  modalRevisionAbierto = signal(false);
  revisionCargando = signal(false);
  revision = signal<EventoRevisionPanelDTO | null>(null);
  eventoRevisionSeleccionado = signal<ProfesorEventoTarjetaDTO | null>(null);

  ngOnInit(): void {
    this.recargarPanel();
  }

  toggleSidebar(): void {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
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
      case 'PENDIENTES':
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
    if (tab === 'PENDIENTES') return p.pendientesCierre?.length ?? 0;
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
    const full = `${n} ${a}`.trim();
    return full || 'Sin asignar';
  }

  horarioRango(inicio?: string, fin?: string): string {
    if (!inicio || !fin) return '—';
    const fmt = (iso: string) => {
      const d = new Date(iso);
      return d.toLocaleTimeString('es-CO', { hour: '2-digit', minute: '2-digit' });
    };
    return `${fmt(inicio)} – ${fmt(fin)}`;
  }

  verAsistenciaEnVivo(): void {
    this.router.navigate(['/admin/check-in']);
  }

  iniciarRevision(ev: ProfesorEventoTarjetaDTO): void {
    this.ejecutarAccion(ev.idEvento, () => this.eventoService.iniciarRevision(ev.idEvento), 'Revisión iniciada.');
  }

  abrirRevisionCierre(ev: ProfesorEventoTarjetaDTO): void {
    if (ev.requiereIniciarRevision) {
      if (!confirm('Debe iniciar la revisión académica antes de emitir certificados. ¿Iniciar ahora?')) {
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
    this.revisionCargando.set(true);
    this.revision.set(null);
    this.eventoService.obtenerRevisionCierre(ev.idEvento).subscribe({
      next: data => {
        this.revision.set(data);
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
  }

  confirmarCierreDesdeModal(): void {
    const ev = this.eventoRevisionSeleccionado();
    if (!ev) return;
    if (
      !confirm(
        '¿Confirmar cierre y generar certificados PDF para todos los alumnos elegibles? Esta acción congela el evento.'
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
      this.tabActiva.set('PENDIENTES');
    } else if ((p.historial?.length ?? 0) > 0) {
      this.tabActiva.set('HISTORIAL');
    }
  }
}
