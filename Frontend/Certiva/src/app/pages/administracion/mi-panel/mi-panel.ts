import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../Services/auth.service';
import { EventoService } from '../../../Services/evento.service';
import { ProfesorPanelDTO } from '../../../Models/portal-dto';
import { etiquetaTipoEvento } from '../../../constants/ui-labels';

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
  sidebarCollapsed = false;

  ngOnInit(): void {
    this.eventoService.obtenerPanelProfesor().subscribe({
      next: data => {
        this.panel.set(data);
        this.loading.set(false);
      },
      error: err => {
        this.loading.set(false);
        this.errorMsg.set(err?.error?.mensaje || 'No se pudo cargar el panel.');
      },
    });
  }

  toggleSidebar(): void {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  etiquetaTipo(tipo?: string): string {
    return etiquetaTipoEvento(tipo);
  }
}
