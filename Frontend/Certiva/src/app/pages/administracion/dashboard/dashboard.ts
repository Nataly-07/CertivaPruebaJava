import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../Services/auth.service';
import { DashboardService } from '../../../Services/dashboard.service';
import { DashboardActivityDTO, DashboardDTO } from '../../../Models/dashboard-dto';
import { DashboardKpiCard } from './components/dashboard-kpi-card/dashboard-kpi-card';
import { DashboardRolesDoughnut } from './components/dashboard-roles-doughnut/dashboard-roles-doughnut';
import { DashboardActivityChart } from './components/dashboard-activity-chart/dashboard-activity-chart';
import { etiquetaRol } from '../../../constants/ui-labels';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    RouterLinkActive,
    DashboardKpiCard,
    DashboardRolesDoughnut,
    DashboardActivityChart,
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard implements OnInit {
  stats: DashboardDTO | null = null;
  activity: DashboardActivityDTO | null = null;
  rangoActividad: 7 | 30 | 90 = 7;
  sidebarCollapsed = false;
  loadingStats = false;
  loadingActivity = false;
  errorMsg: string | null = null;

  /** Texto de tendencia de ejemplo alineado con la referencia visual (sin serie histórica en API). */
  readonly kpiTrends = ['+8.4% vs mes anterior', '+3.1% vs mes anterior', '-1.2% vs mes anterior', '+5.0% vs mes anterior'] as const;

  constructor(
    public authService: AuthService,
    private dashboardService: DashboardService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadStats();
    this.loadActivity();
  }

  get usuario() {
    return this.authService.getUsuario();
  }

  get nombreRolUsuario(): string {
    return etiquetaRol(this.usuario?.rol?.nombre ?? this.usuario?.rol?.codigo ?? '');
  }

  get isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  toggleSidebar(): void {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  onRangoActividadChange(rango: 7 | 30 | 90): void {
    this.rangoActividad = rango;
    this.loadActivity();
  }

  private loadStats(): void {
    this.loadingStats = true;
    this.errorMsg = null;
    this.dashboardService.obtenerEstadisticas().subscribe({
      next: (data) => {
        this.stats = data;
        this.loadingStats = false;
      },
      error: (err) => {
        console.error(err);
        this.loadingStats = false;
        this.errorMsg = err?.status === 403 ? 'No autorizado.' : 'No se pudieron cargar las estadísticas.';
      },
    });
  }

  private loadActivity(): void {
    this.loadingActivity = true;
    this.dashboardService.obtenerActividad(this.rangoActividad).subscribe({
      next: (data) => {
        this.activity = data;
        this.loadingActivity = false;
      },
      error: (err) => {
        console.error(err);
        this.loadingActivity = false;
      },
    });
  }
}
