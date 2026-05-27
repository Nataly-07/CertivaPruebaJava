import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CertificadoService } from '../../../Services/certificado.service';
import {
  CertificadoAdminFilaDTO,
  CertificadosAdminKpiDTO,
  CertificadosAdminVistaDTO,
  EventoOpcionFiltroDTO,
} from '../../../Models/certificado-dto';
import { AdminSidebarComponent } from '../../../Components/admin-sidebar/admin-sidebar.component';
import { DashboardKpiCard } from '../dashboard/components/dashboard-kpi-card/dashboard-kpi-card';

@Component({
  selector: 'app-certificados',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, AdminSidebarComponent, DashboardKpiCard],
  templateUrl: './certificados.html',
  styleUrl: './certificados.scss',
})
export class Certificados implements OnInit {
  vista: CertificadosAdminVistaDTO | null = null;
  certificados: CertificadoAdminFilaDTO[] = [];
  eventosFiltro: EventoOpcionFiltroDTO[] = [];
  kpis: CertificadosAdminKpiDTO | null = null;

  loading = false;
  errorMessage = '';
  sidebarCollapsed = false;

  filtroBusqueda = '';
  filtroEventoId: number | null = null;

  revocandoId: number | null = null;

  constructor(private certificadoService: CertificadoService) {}

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.loading = true;
    this.errorMessage = '';
    this.certificadoService.obtenerVistaAdmin(this.filtroBusqueda, this.filtroEventoId).subscribe({
      next: (data) => {
        this.vista = data;
        this.kpis = data.kpis;
        this.certificados = data.certificados ?? [];
        this.eventosFiltro = data.eventosFiltro ?? [];
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'No se pudo cargar la central de certificados.';
      },
    });
  }

  limpiarFiltros(): void {
    this.filtroBusqueda = '';
    this.filtroEventoId = null;
    this.cargar();
  }

  get ultimoLoteLinea(): string {
    if (!this.kpis?.ultimoEventoNombre) {
      return this.kpis?.ultimaEmisionTexto ?? '—';
    }
    return `${this.kpis.ultimaEmisionTexto} · ${this.kpis.ultimoEventoNombre}`;
  }

  verCertificado(c: CertificadoAdminFilaDTO): void {
    const codigo = c.codigoValidacion || c.codigoMostrar;
    window.open(`/verificar-certificado?codigo=${encodeURIComponent(codigo)}`, '_blank', 'noopener');
  }

  descargarPdf(c: CertificadoAdminFilaDTO): void {
    this.certificadoService.descargarPdfAdmin(c.idCertificado).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `certificado-${c.codigoMostrar || c.idCertificado}.pdf`;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: () => {
        this.errorMessage = 'No se pudo descargar el PDF.';
      },
    });
  }

  revocar(c: CertificadoAdminFilaDTO): void {
    if (c.estado === 'REVOCADO') {
      return;
    }
    const ok = window.confirm(
      `¿Revocar el certificado ${c.codigoMostrar} de ${c.nombreParticipante}? Esta acción invalidará la verificación pública.`
    );
    if (!ok) {
      return;
    }
    this.revocandoId = c.idCertificado;
    this.certificadoService.revocarAdmin(c.idCertificado).subscribe({
      next: (actualizado) => {
        this.revocandoId = null;
        const idx = this.certificados.findIndex((x) => x.idCertificado === c.idCertificado);
        if (idx >= 0) {
          this.certificados[idx] = actualizado;
        }
      },
      error: () => {
        this.revocandoId = null;
        this.errorMessage = 'No se pudo revocar el certificado.';
      },
    });
  }
}
