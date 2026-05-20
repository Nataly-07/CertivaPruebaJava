import {
  AfterViewInit,
  Component,
  ElementRef,
  Input,
  OnChanges,
  OnDestroy,
  SimpleChanges,
  ViewChild,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ArcElement,
  Chart,
  ChartConfiguration,
  DoughnutController,
  Legend,
  Tooltip,
} from 'chart.js';

Chart.register(DoughnutController, ArcElement, Tooltip, Legend);

/** Orden fijo alineado con leyenda de diseño y respuesta del backend. */
const ROL_LABELS = ['Estudiantes', 'Profesores', 'Monitores', 'Administradores'] as const;

const ROL_COLORS = {
  background: [
    'rgba(124, 58, 237, 0.88)', // purple
    'rgba(20, 184, 166, 0.88)', // teal
    'rgba(59, 130, 246, 0.88)', // blue
    'rgba(249, 115, 22, 0.88)', // orange
  ],
};

@Component({
  selector: 'app-dashboard-roles-doughnut',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard-roles-doughnut.html',
  styleUrl: './dashboard-roles-doughnut.scss',
})
export class DashboardRolesDoughnut implements AfterViewInit, OnChanges, OnDestroy {
  @ViewChild('canvasEl') canvasRef?: ElementRef<HTMLCanvasElement>;
  @Input() distribucionRoles: Record<string, number> | null = null;
  @Input() totalUsuarios: number | null | undefined = null;

  private chart?: Chart;

  ngAfterViewInit(): void {
    this.renderSoon();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['distribucionRoles'] || changes['totalUsuarios']) {
      this.renderSoon();
    }
  }

  ngOnDestroy(): void {
    this.chart?.destroy();
  }

  get centroNumero(): number {
    if (this.totalUsuarios != null && this.totalUsuarios >= 0) {
      return this.totalUsuarios;
    }
    const raw = this.distribucionRoles ?? {};
    return ROL_LABELS.reduce((acc, k) => acc + (Number(raw[k]) || 0), 0);
  }

  private renderSoon(): void {
    queueMicrotask(() => {
      if (!this.canvasRef?.nativeElement) return;
      this.render();
    });
  }

  private render(): void {
    const canvas = this.canvasRef?.nativeElement;
    if (!canvas) return;

    const raw = this.distribucionRoles ?? {};
    const labels = [...ROL_LABELS];
    const data = ROL_LABELS.map((k) => Number(raw[k]) || 0);

    this.chart?.destroy();

    const cfg: ChartConfiguration<'doughnut'> = {
      type: 'doughnut',
      data: {
        labels,
        datasets: [
          {
            data,
            backgroundColor: ROL_COLORS.background,
            borderColor: 'rgba(11, 14, 31, 0.95)',
            borderWidth: 2,
            hoverOffset: 6,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '68%',
        plugins: {
          legend: {
            position: 'bottom',
            labels: {
              color: '#cbd5e1',
              boxWidth: 10,
              boxHeight: 10,
              padding: 14,
              font: { size: 11, weight: 500 },
            },
          },
        },
      },
    };

    this.chart = new Chart(canvas, cfg);
  }
}
