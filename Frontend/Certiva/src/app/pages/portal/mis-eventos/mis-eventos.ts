import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { InscripcionService } from '../../../Services/inscripcion.service';
import { CertificadoService } from '../../../Services/certificado.service';
import { InscripcionPortalDTO } from '../../../Models/portal-dto';
import { QrCodeDisplayComponent } from '../../../Components/qr-code-display/qr-code-display.component';
import { etiquetaTipoEvento } from '../../../constants/ui-labels';

type TabFase = 'INSCRITO' | 'EN_CURSO' | 'FINALIZADO';

@Component({
  selector: 'app-mis-eventos',
  standalone: true,
  imports: [CommonModule, DatePipe, QrCodeDisplayComponent],
  template: `
    <div class="page-card glass-card">
      <h2 class="page-heading">Mis eventos</h2>
      <p class="page-sub">Inscritos, en curso y finalizados — su pase QR de registro para el check-in.</p>

      <div class="tabs" role="tablist">
        @for (t of tabs; track t.id) {
          <button
            type="button"
            class="tab-btn"
            [class.active]="tabActiva() === t.id"
            (click)="tabActiva.set(t.id)"
          >
            {{ t.label }}
            <span class="tab-count">{{ contar(t.id) }}</span>
          </button>
        }
      </div>

      @if (loading()) {
        <p class="text-secondary py-4">Cargando…</p>
      } @else if (errorMsg()) {
        <div class="alert alert-warning">{{ errorMsg() }}</div>
      } @else if (filtradas().length === 0) {
        <p class="text-secondary py-4">No hay eventos en esta sección.</p>
      } @else {
        <div class="event-grid">
          @for (ins of filtradas(); track ins.idInscripcion) {
            <article class="event-item">
              <div class="event-item-head">
                <span class="tipo-pill">{{ etiquetaTipo(ins.tipoEvento) }}</span>
                <span class="estado-pill">{{ ins.estado }}</span>
              </div>
              <h3>{{ ins.nombreEvento }}</h3>
              <p class="meta">
                {{ ins.fechaInicio | date: 'mediumDate' }} — {{ ins.fechaFin | date: 'mediumDate' }}
              </p>
              @if (ins.tokenQr && tabActiva() !== 'FINALIZADO') {
                <div class="qr-block">
                  <p class="small text-muted mb-2">QR de registro (pase de entrada)</p>
                  <app-qr-code-display [data]="ins.tokenQr" [size]="140" caption="Presentar en la entrada" />
                </div>
              }
              @if (tabActiva() === 'FINALIZADO') {
                @if (ins.puedeDescargarCertificado) {
                  <button
                    type="button"
                    class="btn btn-gradient btn-sm mt-2"
                    [disabled]="descargandoId() === ins.idInscripcion"
                    (click)="descargarCertificado(ins)"
                  >
                    Descargar certificado
                  </button>
                } @else if (ins.motivoCertificadoPendiente) {
                  <p class="small text-warning mt-2 mb-0">{{ ins.motivoCertificadoPendiente }}</p>
                }
              }
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
      .tabs {
        display: flex;
        flex-wrap: wrap;
        gap: 0.5rem;
        margin-bottom: 1.25rem;
      }
      .tab-btn {
        border: 1px solid rgba(255, 255, 255, 0.12);
        background: rgba(255, 255, 255, 0.04);
        color: var(--text-secondary);
        border-radius: var(--radius-md);
        padding: 0.45rem 0.85rem;
        font-weight: 600;
        font-size: 0.85rem;
        cursor: pointer;
        transition: all 0.2s ease;
      }
      .tab-btn.active {
        border-color: rgba(124, 58, 237, 0.5);
        background: rgba(124, 58, 237, 0.15);
        color: var(--text-primary);
      }
      .tab-count {
        opacity: 0.7;
        margin-left: 0.25rem;
      }
      .event-grid {
        display: grid;
        gap: 1rem;
      }
      .event-item {
        border: 1px solid rgba(255, 255, 255, 0.08);
        border-radius: var(--radius-md);
        padding: 1.1rem 1.15rem;
        background: rgba(0, 0, 0, 0.15);
      }
      .event-item-head {
        display: flex;
        gap: 0.5rem;
        margin-bottom: 0.5rem;
      }
      .tipo-pill,
      .estado-pill {
        font-size: 0.68rem;
        font-weight: 700;
        text-transform: uppercase;
        letter-spacing: 0.06em;
        padding: 0.2rem 0.5rem;
        border-radius: 999px;
      }
      .tipo-pill {
        background: rgba(34, 211, 238, 0.12);
        color: var(--accent-cyan);
      }
      .estado-pill {
        background: rgba(255, 255, 255, 0.08);
        color: var(--text-secondary);
      }
      .event-item h3 {
        font-size: 1.05rem;
        font-weight: 700;
        margin-bottom: 0.35rem;
      }
      .meta {
        font-size: 0.82rem;
        color: var(--text-secondary);
        margin-bottom: 0.75rem;
      }
      .qr-block {
        margin-top: 0.75rem;
        padding-top: 0.75rem;
        border-top: 1px solid rgba(255, 255, 255, 0.06);
      }
    `,
  ],
})
export class MisEventos implements OnInit {
  private inscripcionService = inject(InscripcionService);
  private certificadoService = inject(CertificadoService);

  readonly tabs: { id: TabFase; label: string }[] = [
    { id: 'INSCRITO', label: 'Inscritos' },
    { id: 'EN_CURSO', label: 'En curso' },
    { id: 'FINALIZADO', label: 'Finalizados' },
  ];

  tabActiva = signal<TabFase>('INSCRITO');
  lista = signal<InscripcionPortalDTO[]>([]);
  loading = signal(true);
  errorMsg = signal<string | null>(null);
  descargandoId = signal<number | null>(null);

  ngOnInit(): void {
    this.inscripcionService.listarMis().subscribe({
      next: data => {
        this.lista.set(data);
        this.loading.set(false);
      },
      error: err => {
        this.loading.set(false);
        this.errorMsg.set(err?.error?.mensaje || 'No se pudieron cargar sus inscripciones.');
      },
    });
  }

  filtradas(): InscripcionPortalDTO[] {
    return this.lista().filter(i => i.fase === this.tabActiva());
  }

  contar(fase: TabFase): number {
    return this.lista().filter(i => i.fase === fase).length;
  }

  etiquetaTipo(tipo?: string): string {
    return etiquetaTipoEvento(tipo);
  }

  descargarCertificado(ins: InscripcionPortalDTO): void {
    this.descargandoId.set(ins.idInscripcion);
    if (ins.idCertificado) {
      this.certificadoService.descargarPdfMi(ins.idCertificado).subscribe({
        next: blob => this.guardarBlob(blob, ins.nombreEvento),
        error: () => this.descargandoId.set(null),
        complete: () => this.descargandoId.set(null),
      });
      return;
    }

    this.certificadoService.emitirPorInscripcion(ins.idInscripcion).subscribe({
      next: cert => {
        this.certificadoService.descargarPdfMi(cert.idCertificado).subscribe({
          next: blob => this.guardarBlob(blob, ins.nombreEvento),
          complete: () => this.descargandoId.set(null),
        });
      },
      error: err => {
        this.descargandoId.set(null);
        this.errorMsg.set(err?.error?.mensaje || 'No se pudo generar el certificado.');
      },
    });
  }

  private guardarBlob(blob: Blob, nombre: string): void {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `certificado-${nombre.replace(/\s+/g, '-').toLowerCase()}.pdf`;
    a.click();
    URL.revokeObjectURL(url);
  }
}
