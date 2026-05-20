import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CertificadoService } from '../../../Services/certificado.service';
import { CertificadoPortalDTO } from '../../../Models/portal-dto';

@Component({
  selector: 'app-mis-certificados',
  standalone: true,
  imports: [CommonModule, DatePipe, RouterLink],
  template: `
    <div class="page-card glass-card">
      <h2 class="page-heading">Mis certificados</h2>
      <p class="page-sub">
        Diplomas emitidos con código de verificación público. Cualquier reclutador puede validarlos en
        <a routerLink="/verificar-certificado">verificar certificado</a>.
      </p>

      @if (loading()) {
        <p class="text-secondary py-4">Cargando…</p>
      } @else if (lista().length === 0) {
        <p class="text-secondary py-4">
          Aún no tiene certificados emitidos. Al finalizar un evento y cumplir los requisitos, podrá descargarlos desde
          <a routerLink="/portal/mis-eventos">Mis eventos</a>.
        </p>
      } @else {
        <div class="cert-grid">
          @for (c of lista(); track c.idCertificado) {
            <article class="cert-item">
              <h3>{{ c.nombreEvento }}</h3>
              <p class="meta">Emitido: {{ c.fechaEmision | date: 'medium' }}</p>
              <p class="codigo small text-muted">Código: {{ c.codigoValidacion }}</p>
              <button
                type="button"
                class="btn btn-gradient btn-sm mt-2"
                [disabled]="descargandoId() === c.idCertificado"
                (click)="descargar(c)"
              >
                Descargar PDF
              </button>
            </article>
          }
        </div>
      }
    </div>
  `,
  styles: [
    `
      .page-card {
        padding: 1.75rem;
        border-radius: var(--radius-lg);
      }
      .page-heading {
        font-size: 1.35rem;
        font-weight: 800;
        margin-bottom: 0.35rem;
      }
      .page-sub {
        color: var(--text-secondary);
        font-size: 0.9rem;
        margin-bottom: 1.5rem;
      }
      .page-sub a {
        color: var(--accent-cyan);
      }
      .cert-grid {
        display: grid;
        gap: 1rem;
      }
      .cert-item {
        border: 1px solid rgba(124, 58, 237, 0.25);
        border-radius: var(--radius-md);
        padding: 1.1rem 1.15rem;
        background: rgba(124, 58, 237, 0.06);
      }
      .cert-item h3 {
        font-size: 1.05rem;
        font-weight: 700;
        margin-bottom: 0.35rem;
      }
      .meta {
        font-size: 0.82rem;
        color: var(--text-secondary);
      }
    `,
  ],
})
export class MisCertificados implements OnInit {
  private certificadoService = inject(CertificadoService);

  lista = signal<CertificadoPortalDTO[]>([]);
  loading = signal(true);
  descargandoId = signal<number | null>(null);

  ngOnInit(): void {
    this.certificadoService.listarMis().subscribe({
      next: data => {
        this.lista.set(data);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  descargar(c: CertificadoPortalDTO): void {
    this.descargandoId.set(c.idCertificado);
    this.certificadoService.descargarPdfMi(c.idCertificado).subscribe({
      next: blob => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `certificado-${c.codigoValidacion.slice(0, 8)}.pdf`;
        a.click();
        URL.revokeObjectURL(url);
      },
      complete: () => this.descargandoId.set(null),
    });
  }
}
