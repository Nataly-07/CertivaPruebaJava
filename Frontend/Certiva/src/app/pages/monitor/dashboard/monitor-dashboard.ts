import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../Services/auth.service';
import { EventoService } from '../../../Services/evento.service';
import {
  EventoAsistenciaEnVivoDTO,
  MonitorEventoTarjetaDTO,
  MonitorPanelDTO,
  ProfesorAlumnoAsistenciaDTO,
} from '../../../Models/portal-dto';
import { mensajeErrorHttp } from '../../../utils/http-error-message';
import { MonitorSidebarComponent } from '../../../Components/monitor-sidebar/monitor-sidebar.component';
import { etiquetaAlertaMonitor, etiquetaEstadoMonitor } from '../../../constants/monitor-ui';

type VistaMonitor = 'dashboard' | 'hoy' | 'reportes';

@Component({
  selector: 'app-monitor-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, MonitorSidebarComponent],
  templateUrl: './monitor-dashboard.html',
  styleUrl: './monitor-dashboard.scss',
})
export class MonitorDashboard implements OnInit, OnDestroy {
  private authService = inject(AuthService);
  private eventoService = inject(EventoService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  panel = signal<MonitorPanelDTO | null>(null);
  loading = signal(true);
  errorMsg = signal<string | null>(null);
  accionMsg = signal<string | null>(null);
  sidebarCollapsed = false;
  vista = signal<VistaMonitor>('dashboard');

  drawerAbierto = signal(false);
  drawerCargando = signal(false);
  matriz = signal<EventoAsistenciaEnVivoDTO | null>(null);
  eventoSeleccionado = signal<MonitorEventoTarjetaDTO | null>(null);

  private refreshTimer: ReturnType<typeof setInterval> | null = null;

  ngOnInit(): void {
    this.route.data.subscribe((data) => {
      this.vista.set((data['vista'] as VistaMonitor) ?? 'dashboard');
    });
    this.recargarPanel();
    this.refreshTimer = setInterval(() => this.recargarPanel(true), 30_000);
  }

  ngOnDestroy(): void {
    if (this.refreshTimer) {
      clearInterval(this.refreshTimer);
    }
  }

  get usuario() {
    return this.authService.getUsuario();
  }

  nombreMonitor(): string {
    const u = this.usuario;
    if (!u) return 'Monitor';
    return `${u.nombres} ${u.apellidos}`.trim();
  }

  recargarPanel(silencioso = false): void {
    if (!silencioso) {
      this.loading.set(true);
    }
    this.errorMsg.set(null);
    this.eventoService.obtenerPanelMonitor().subscribe({
      next: (data) => {
        this.panel.set(data);
        this.loading.set(false);
        const sel = this.eventoSeleccionado();
        if (sel && this.drawerAbierto()) {
          const actualizado = data.eventos?.find((e) => e.idEvento === sel.idEvento);
          if (actualizado) {
            this.eventoSeleccionado.set(actualizado);
          }
        }
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMsg.set(mensajeErrorHttp(err, 'No se pudo cargar el panel operativo.'));
      },
    });
  }

  eventosVisibles(): MonitorEventoTarjetaDTO[] {
    const p = this.panel();
    if (!p?.eventos) return [];
    return p.eventos;
  }

  tituloSeccion(): string {
    if (this.vista() === 'reportes') return 'Reportes operativos';
    return 'Eventos y sesiones de hoy';
  }

  etiquetaEstado = etiquetaEstadoMonitor;
  etiquetaAlerta = etiquetaAlertaMonitor;

  textoSalon(ev: MonitorEventoTarjetaDTO): string {
    const u = (ev.ubicacion ?? '').trim();
    return u || 'Salón por confirmar';
  }

  textoProgreso(ev: MonitorEventoTarjetaDTO): string {
    const y = ev.sesionesTotales ?? 1;
    const x = ev.sesionActual ?? 0;
    const pct = ev.porcentajeCheckIn ?? 0;
    if (ev.estadoOperativo === 'PROXIMO') {
      return `Sesión ${x}/${y} · Inscritos: ${ev.inscritosActivos}`;
    }
    return `Sesión ${x}/${y} · Check-in: ${ev.asistenciasConfirmadas}/${ev.inscritosActivos} (${pct}%)`;
  }

  textoTiempo(ev: MonitorEventoTarjetaDTO): string {
    if (ev.estadoOperativo === 'PROXIMO' && ev.fechaInicio) {
      const d = new Date(ev.fechaInicio);
      return `Horario: ${d.toLocaleTimeString('es-CO', { hour: '2-digit', minute: '2-digit' })}`;
    }
    if (ev.minutosHastaFin != null && ev.minutosHastaFin > 0) {
      return `Tiempo hasta fin: ${ev.minutosHastaFin} min`;
    }
    return '';
  }

  nombreProfesor(ev: MonitorEventoTarjetaDTO): string {
    const n = (ev.profesorNombre ?? '').trim();
    const a = (ev.profesorApellidos ?? '').trim();
    return `${n} ${a}`.trim() || 'Organizador';
  }

  claseTarjeta(ev: MonitorEventoTarjetaDTO): string {
    if (ev.nivelAlerta === 'CRITICO') return 'mon-evento--critico';
    if (ev.nivelAlerta === 'ADVERTENCIA') return 'mon-evento--advertencia';
    return 'mon-evento--normal';
  }

  claseBadgeEstado(ev: MonitorEventoTarjetaDTO): string {
    if (ev.estadoOperativo === 'EN_CURSO') return 'estado-en-curso';
    if (ev.estadoOperativo === 'PROXIMO') return 'estado-registro';
    return 'estado-pendiente';
  }

  abrirMatriz(ev: MonitorEventoTarjetaDTO): void {
    this.eventoSeleccionado.set(ev);
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
    this.eventoSeleccionado.set(null);
  }

  refrescarMatriz(): void {
    const ev = this.eventoSeleccionado();
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

  irCheckIn(): void {
    void this.router.navigate(['/admin/check-in']);
  }

  contactarOrganizador(ev: MonitorEventoTarjetaDTO): void {
    const correo = ev.profesorCorreo?.trim();
    if (!correo) {
      this.accionMsg.set('No hay correo del organizador registrado para este evento.');
      return;
    }
    const asunto = encodeURIComponent(`[Certiva] Soporte operativo — ${ev.nombreEvento}`);
    const cuerpo = encodeURIComponent(
      `Hola ${this.nombreProfesor(ev)},\n\nSoy ${this.nombreMonitor()}, monitor asignado al evento «${ev.nombreEvento}».\n\n`
    );
    window.location.href = `mailto:${correo}?subject=${asunto}&body=${cuerpo}`;
  }

  esElegible(al: ProfesorAlumnoAsistenciaDTO): boolean {
    const umbral = this.matriz()?.porcentajeAsistenciaMinimo ?? 80;
    return al.porcentajeAsistencia >= umbral;
  }
}
