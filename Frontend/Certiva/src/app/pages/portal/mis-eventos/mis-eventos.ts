import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { InscripcionService } from '../../../Services/inscripcion.service';
import { CertificadoService } from '../../../Services/certificado.service';
import { InscripcionPortalDTO } from '../../../Models/portal-dto';
import { QrCodeDisplayComponent } from '../../../Components/qr-code-display/qr-code-display.component';
import { contenidoQrInscripcion } from '../../../utils/inscripcion-qr';
import { etiquetaModalidad, etiquetaTipoEvento } from '../../../constants/ui-labels';

type TabFase = 'INSCRITO' | 'EN_CURSO' | 'FINALIZADO';

@Component({
  selector: 'app-mis-eventos',
  standalone: true,
  imports: [CommonModule, DatePipe, RouterLink, QrCodeDisplayComponent],
  templateUrl: './mis-eventos.html',
  styleUrl: './mis-eventos.scss',
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
  qrModal = signal<InscripcionPortalDTO | null>(null);

  ngOnInit(): void {
    this.inscripcionService.listarMis().subscribe({
      next: data => {
        this.lista.set(data);
        this.loading.set(false);
        this.seleccionarPrimeraPestañaConDatos();
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

  cambiarTab(fase: TabFase): void {
    this.tabActiva.set(fase);
  }

  etiquetaTipo(tipo?: string): string {
    return etiquetaTipoEvento(tipo);
  }

  etiquetaMod(modalidad?: string): string {
    return etiquetaModalidad(modalidad);
  }

  claseTipo(tipo?: string): string {
    const t = (tipo ?? 'curso').toLowerCase();
    return `badge-tipo badge-tipo--${t}`;
  }

  claseModalidad(modalidad?: string): string {
    const m = (modalidad ?? 'presencial').toLowerCase();
    return `badge-mod badge-mod--${m}`;
  }

  progresoPct(ins: InscripcionPortalDTO): number {
    if (ins.porcentajeProgreso != null) {
      return Math.min(100, Math.max(0, ins.porcentajeProgreso));
    }
    const total = ins.sesionesTotales ?? 0;
    const hechas = ins.sesionesAsistidas ?? 0;
    if (total <= 0) {
      return 0;
    }
    return Math.min(100, Math.round((hechas / total) * 100));
  }

  textoProgreso(ins: InscripcionPortalDTO): string {
    const pct = this.progresoPct(ins);
    const hechas = ins.sesionesAsistidas ?? 0;
    const total = ins.sesionesTotales ?? 0;
    return `${pct}% · ${hechas}/${total} sesiones`;
  }

  textoQr(ins: InscripcionPortalDTO): string {
    return contenidoQrInscripcion(ins.idInscripcion, ins.tokenQr);
  }

  abrirQr(ins: InscripcionPortalDTO): void {
    if (ins.idInscripcion) {
      this.qrModal.set(ins);
    }
  }

  cerrarQr(): void {
    this.qrModal.set(null);
  }

  tieneEnlaceSesion(ins: InscripcionPortalDTO): boolean {
    return !!ins.enlaceVirtual?.trim();
  }

  descargarCertificado(ins: InscripcionPortalDTO, ev?: Event): void {
    ev?.stopPropagation();
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

  private seleccionarPrimeraPestañaConDatos(): void {
    const orden: TabFase[] = ['EN_CURSO', 'INSCRITO', 'FINALIZADO'];
    for (const f of orden) {
      if (this.contar(f) > 0) {
        this.tabActiva.set(f);
        return;
      }
    }
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
