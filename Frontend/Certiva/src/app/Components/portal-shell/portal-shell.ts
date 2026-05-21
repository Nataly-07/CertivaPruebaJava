import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../Services/auth.service';

@Component({
  selector: 'app-portal-shell',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="portal-layout">
      <header class="portal-top glass-card">
        <div class="portal-brand">
          <span class="portal-badge">Estudiante / Participante</span>
          <h1 class="portal-logo">Certiva</h1>
        </div>
        <nav class="portal-nav" aria-label="Navegación del portal">
          <a routerLink="/portal" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }">Inicio</a>
          <a routerLink="/portal/mis-eventos" routerLinkActive="active">Mis eventos</a>
          <a routerLink="/portal/certificados" routerLinkActive="active">Certificados</a>
          <a routerLink="/portal/eventos" routerLinkActive="active">Explorar</a>
        </nav>
        <button type="button" class="btn btn-dark-outline btn-sm" (click)="logout()">Salir</button>
      </header>
      <main class="portal-main">
        <router-outlet />
      </main>
    </div>
  `,
  styles: [
    `
      .portal-layout {
        min-height: 100vh;
        background: var(--gradient-bg);
        padding: 1.5rem 1.25rem 3rem;
      }
      .portal-top {
        max-width: 1200px;
        margin: 0 auto 1.75rem;
        padding: 1rem 1.25rem;
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        gap: 1rem;
        justify-content: space-between;
      }
      .portal-brand {
        display: flex;
        flex-direction: column;
        gap: 0.25rem;
      }
      .portal-badge {
        font-size: 0.68rem;
        font-weight: 700;
        letter-spacing: 0.1em;
        text-transform: uppercase;
        color: var(--accent-cyan);
      }
      .portal-logo {
        font-size: 1.25rem;
        font-weight: 800;
        margin: 0;
        color: var(--text-primary);
      }
      .portal-nav {
        display: flex;
        flex-wrap: wrap;
        gap: 0.5rem 1rem;
      }
      .portal-nav a {
        color: var(--text-secondary);
        text-decoration: none;
        font-size: 0.9rem;
        font-weight: 600;
        padding: 0.35rem 0.5rem;
        border-radius: var(--radius-sm);
        transition: color 0.2s ease, background 0.2s ease;
      }
      .portal-nav a:hover,
      .portal-nav a.active {
        color: var(--accent-cyan);
        background: rgba(34, 211, 238, 0.08);
      }
      .portal-main {
        max-width: 1200px;
        margin: 0 auto;
        width: 100%;
      }
    `,
  ],
})
export class PortalShell {
  constructor(
    private auth: AuthService,
    private router: Router
  ) {}

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
