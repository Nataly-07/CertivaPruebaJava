import {
  AfterViewInit,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  Output,
  SimpleChanges,
  ViewChild,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  BarController,
  BarElement,
  CategoryScale,
  Chart,
  ChartConfiguration,
  Legend,
  LinearScale,
  Tooltip,
} from 'chart.js';
import { DashboardActivityDTO } from '../../../../../Models/dashboard-dto';

Chart.register(BarController, BarElement, CategoryScale, LinearScale, Tooltip, Legend);

@Component({
  selector: 'app-dashboard-activity-chart',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard-activity-chart.html',
  styleUrl: './dashboard-activity-chart.scss',
})
export class DashboardActivityChart implements AfterViewInit, OnChanges, OnDestroy {
  @ViewChild('canvasEl') canvasRef?: ElementRef<HTMLCanvasElement>;
  @Input() activity: DashboardActivityDTO | null = null;
  @Input() rango: 7 | 30 | 90 = 7;
  @Output() readonly rangoChange = new EventEmitter<7 | 30 | 90>();

  private chart?: Chart;

  ngAfterViewInit(): void {
    this.renderSoon();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['activity']) {
      this.renderSoon();
    }
  }

  ngOnDestroy(): void {
    this.chart?.destroy();
  }

  setRango(nuevo: 7 | 30 | 90): void {
    if (nuevo === this.rango) return;
    this.rangoChange.emit(nuevo);
  }

  private renderSoon(): void {
    queueMicrotask(() => {
      if (!this.canvasRef?.nativeElement) return;
      this.render();
    });
  }

  private render(): void {
    const canvas = this.canvasRef!.nativeElement;
    const puntos = this.activity?.puntos ?? [];
    const labels = puntos.map((p) => {
      const d = new Date(p.fecha);
      if (Number.isNaN(d.getTime())) {
        return String(p.fecha);
      }
      return d.toLocaleDateString('es-ES', { day: '2-digit', month: 'short' });
    });
    const asistencias = puntos.map((p) => p.asistencias ?? 0);
    const certificados = puntos.map((p) => p.certificados ?? 0);

    this.chart?.destroy();

    const cfg: ChartConfiguration<'bar'> = {
      type: 'bar',
      data: {
        labels,
        datasets: [
          {
            label: 'Asistencias',
            data: asistencias,
            backgroundColor: 'rgba(20, 184, 166, 0.72)',
            borderRadius: 6,
            maxBarThickness: 26,
          },
          {
            label: 'Certificados',
            data: certificados,
            backgroundColor: 'rgba(59, 130, 246, 0.72)',
            borderRadius: 6,
            maxBarThickness: 26,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom',
            align: 'start',
            labels: {
              color: '#cbd5e1',
              usePointStyle: true,
              pointStyle: 'rectRounded',
              padding: 16,
              font: { size: 12, weight: 500 },
            },
          },
        },
        scales: {
          x: {
            ticks: { color: '#94a3b8', maxRotation: 45, minRotation: 0 },
            grid: { color: 'rgba(148, 163, 184, 0.06)' },
          },
          y: {
            beginAtZero: true,
            ticks: { color: '#94a3b8', precision: 0 },
            grid: { color: 'rgba(148, 163, 184, 0.1)' },
          },
        },
      },
    };

    this.chart = new Chart(canvas, cfg);
  }
}
