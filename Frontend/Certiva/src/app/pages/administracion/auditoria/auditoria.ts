import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuditoriaService } from '../../../Services/auditoria.service';
import { AuditoriaResumenDTO } from '../../../Models/auditoria-dto';
import { AdminSidebarComponent } from '../../../Components/admin-sidebar/admin-sidebar.component';

@Component({
  selector: 'app-auditoria',
  standalone: true,
  imports: [CommonModule, FormsModule, AdminSidebarComponent],
  templateUrl: './auditoria.html',
  styleUrl: './auditoria.scss',
})
export class AuditoriaPage implements OnInit {
  registros: AuditoriaResumenDTO[] = [];
  loading = false;
  errorMessage = '';
  sidebarCollapsed = false;

  filtroAccion = '';
  filtroBusqueda = '';
  filtroDesde = '';
  filtroHasta = '';

  readonly accionesDominio = [
    'ROLE_CHANGE',
    'EVENT_CREATED',
    'EVENT_CANCELLED',
    'EVENT_FORCE_CLOSED',
    'EVENT_CLOSED',
    'EVENT_REVISION_STARTED',
    'EVENT_STAFF_REASIGNADO',
    'CHECKIN_SUCCESS',
    'CHECKIN_DENIED',
    'CERTIFICATE_GENERATED',
    'INSCRIPCION_CANCELLED',
    'LOGIN_EXITOSO',
    'LOGIN_FALLIDO',
  ];

  constructor(private auditoriaService: AuditoriaService) {}

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.loading = true;
    this.errorMessage = '';
    this.auditoriaService
      .listarRecientes({
        limite: 250,
        accion: this.filtroAccion || undefined,
        busqueda: this.filtroBusqueda || undefined,
        desde: this.filtroDesde ? `${this.filtroDesde}T00:00:00` : undefined,
        hasta: this.filtroHasta ? `${this.filtroHasta}T23:59:59` : undefined,
      })
      .subscribe({
        next: (data) => {
          this.registros = data;
          this.loading = false;
        },
        error: () => {
          this.loading = false;
          this.errorMessage = 'No se pudo cargar la auditoría.';
        },
      });
  }

  limpiarFiltros(): void {
    this.filtroAccion = '';
    this.filtroBusqueda = '';
    this.filtroDesde = '';
    this.filtroHasta = '';
    this.cargar();
  }

  claseAccion(accion: string): string {
    const a = (accion || '').toUpperCase();
    if (a.includes('DENIED') || a.includes('FALLIDO') || a.includes('CANCEL')) return 'log-warn';
    if (a.includes('SUCCESS') || a.includes('GENERATED') || a.includes('EXITOSO') || a.includes('CREATED')) return 'log-ok';
    if (a.includes('ROLE') || a.includes('FORCE') || a.includes('REASIGN')) return 'log-critical';
    return 'log-neutral';
  }

  etiquetaAccion(accion: string): string {
    return accion || '—';
  }
}
