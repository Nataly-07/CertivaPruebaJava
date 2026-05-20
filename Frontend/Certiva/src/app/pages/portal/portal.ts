import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../Services/auth.service';

@Component({
  selector: 'app-portal',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="portal-home glass-card">
      <p class="welcome">Hola, {{ nombreUsuario }}</p>
      <h2 class="portal-home-title">Tu espacio de aprendizaje</h2>
      <p class="portal-home-lead">
        Consulta tus inscripciones, presenta tu QR de entrada en los eventos y descarga certificados al finalizar.
      </p>
      <div class="quick-links">
        <a routerLink="/portal/mis-eventos" class="quick-card">
          <span class="quick-label">Mis eventos</span>
          <span class="quick-hint">Inscritos, en curso y finalizados</span>
        </a>
        <a routerLink="/portal/certificados" class="quick-card">
          <span class="quick-label">Certificados</span>
          <span class="quick-hint">Descargar diplomas verificables</span>
        </a>
        <a routerLink="/portal/eventos" class="quick-card">
          <span class="quick-label">Explorar eventos</span>
          <span class="quick-hint">Nuevas inscripciones</span>
        </a>
      </div>
    </div>
  `,
  styles: [
    `
      .portal-home {
        padding: 2rem 1.75rem;
        border-radius: var(--radius-lg);
      }
      .welcome {
        font-size: 0.85rem;
        color: var(--accent-cyan);
        font-weight: 600;
        margin-bottom: 0.35rem;
      }
      .portal-home-title {
        font-size: 1.5rem;
        font-weight: 800;
        margin-bottom: 0.5rem;
      }
      .portal-home-lead {
        color: var(--text-secondary);
        max-width: 520px;
        margin-bottom: 1.75rem;
      }
      .quick-links {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
        gap: 1rem;
      }
      .quick-card {
        display: block;
        padding: 1.15rem 1.2rem;
        border-radius: var(--radius-md);
        border: 1px solid rgba(124, 58, 237, 0.3);
        background: rgba(124, 58, 237, 0.08);
        text-decoration: none;
        transition: transform 0.2s ease, border-color 0.2s ease;
      }
      .quick-card:hover {
        transform: translateY(-2px);
        border-color: rgba(34, 211, 238, 0.45);
      }
      .quick-label {
        display: block;
        font-weight: 700;
        color: var(--text-primary);
        margin-bottom: 0.25rem;
      }
      .quick-hint {
        font-size: 0.8rem;
        color: var(--text-secondary);
      }
    `,
  ],
})
export class Portal {
  nombreUsuario = '';

  constructor(private auth: AuthService) {
    const u = this.auth.getUsuario();
    this.nombreUsuario = u ? `${u.nombres}`.trim() : 'Participante';
  }
}
