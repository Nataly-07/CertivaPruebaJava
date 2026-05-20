import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-dashboard-kpi-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard-kpi-card.html',
  styleUrl: './dashboard-kpi-card.scss',
})
export class DashboardKpiCard {
  @Input({ required: true }) label!: string;
  @Input({ required: true }) value: number | null | undefined = 0;
  @Input() icon: 'users' | 'calendar' | 'clipboard' | 'award' = 'users';
  /** Color del valor principal (referencia visual KPI). */
  @Input() valueTone: 'purple' | 'teal' | 'blue' | 'orange' = 'purple';
  /** Texto de tendencia, p. ej. "+8.4% vs mes anterior" (opcional). */
  @Input() trendLine: string | null = null;
}
