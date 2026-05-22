import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../../Services/auth.service';
import { InscripcionService } from '../../Services/inscripcion.service';
import { CertificadoService } from '../../Services/certificado.service';
import { EventoService } from '../../Services/evento.service';
import { CertificadoPortalDTO, InscripcionPortalDTO } from '../../Models/portal-dto';
import { EventoPublico } from '../../Models/evento-publico';
import { etiquetaTipoEvento } from '../../constants/ui-labels';

@Component({
  selector: 'app-portal',
  standalone: true,
  imports: [CommonModule, RouterLink, DatePipe],
  templateUrl: './portal.html',
  styleUrl: './portal.scss',
})
export class Portal implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly inscripcionService = inject(InscripcionService);
  private readonly certificadoService = inject(CertificadoService);
  private readonly eventoService = inject(EventoService);

  readonly etiquetaTipo = etiquetaTipoEvento;

  nombreUsuario = 'Participante';
  loading = signal(true);
  errorMsg = signal<string | null>(null);

  eventosActivosHoy = signal(0);
  proximoEvento = signal<InscripcionPortalDTO | null>(null);
  eventoHoyQr = signal<InscripcionPortalDTO | null>(null);
  totalCertificados = signal(0);
  ultimoCertificado = signal<CertificadoPortalDTO | null>(null);
  eventoRecomendado = signal<EventoPublico | null>(null);

  ngOnInit(): void {
    const u = this.auth.getUsuario();
    this.nombreUsuario = u?.nombres?.trim() || 'Participante';

    forkJoin({
      inscripciones: this.inscripcionService.listarMis().pipe(catchError(() => of([] as InscripcionPortalDTO[]))),
      certificados: this.certificadoService.listarMis().pipe(catchError(() => of([] as CertificadoPortalDTO[]))),
      catalogo: this.eventoService.obtenerCatalogoPublico().pipe(catchError(() => of([] as EventoPublico[]))),
    }).subscribe({
      next: ({ inscripciones, certificados, catalogo }) => {
        this.procesarInscripciones(inscripciones ?? []);
        this.procesarCertificados(certificados ?? []);
        this.eventoRecomendado.set((catalogo ?? [])[0] ?? null);
        this.loading.set(false);
      },
      error: () => {
        this.errorMsg.set('No se pudo cargar tu panel. Intenta recargar la página.');
        this.loading.set(false);
      },
    });
  }

  get saludoDinamico(): string {
    const n = this.eventosActivosHoy();
    if (n === 0) {
      return `Hola, ${this.nombreUsuario}. No tienes eventos activos hoy`;
    }
    if (n === 1) {
      return `Hola, ${this.nombreUsuario}. Tienes 1 evento activo hoy`;
    }
    return `Hola, ${this.nombreUsuario}. Tienes ${n} eventos activos hoy`;
  }

  get textoProximoEvento(): string {
    const p = this.proximoEvento();
    if (!p) {
      return 'Sin eventos próximos. Explora el catálogo para inscribirte.';
    }
    const inicio = this.parseFecha(p.fechaInicio);
    const cuando = inicio
      ? inicio.toLocaleDateString('es', { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' })
      : 'fecha por confirmar';
    return `Próximo: ${p.nombreEvento} — ${cuando}`;
  }

  private procesarInscripciones(lista: InscripcionPortalDTO[]): void {
    const hoy = new Date();
    const vigentes = lista.filter(i => i.fase !== 'FINALIZADO');

    const activosHoy = vigentes.filter(i => this.ocurreHoy(hoy, i));
    this.eventosActivosHoy.set(activosHoy.length);

    const conQrHoy = activosHoy.find(i => !!i.idInscripcion);
    this.eventoHoyQr.set(conQrHoy ?? null);

    const ordenados = [...vigentes].sort((a, b) => {
      const fa = this.parseFecha(a.fechaInicio)?.getTime() ?? Number.MAX_SAFE_INTEGER;
      const fb = this.parseFecha(b.fechaInicio)?.getTime() ?? Number.MAX_SAFE_INTEGER;
      return fa - fb;
    });
    this.proximoEvento.set(ordenados[0] ?? null);
  }

  private procesarCertificados(lista: CertificadoPortalDTO[]): void {
    this.totalCertificados.set(lista.length);
    if (lista.length === 0) {
      this.ultimoCertificado.set(null);
      return;
    }
    const ordenados = [...lista].sort((a, b) => {
      const fa = this.parseFecha(a.fechaEmision)?.getTime() ?? 0;
      const fb = this.parseFecha(b.fechaEmision)?.getTime() ?? 0;
      return fb - fa;
    });
    this.ultimoCertificado.set(ordenados[0]);
  }

  private ocurreHoy(hoy: Date, ins: InscripcionPortalDTO): boolean {
    const inicio = this.parseFecha(ins.fechaInicio);
    const fin = this.parseFecha(ins.fechaFin) ?? inicio;
    if (!inicio) {
      return false;
    }
    const dayStart = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate());
    const dayEnd = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate(), 23, 59, 59, 999);
    return inicio <= dayEnd && (fin ?? inicio) >= dayStart;
  }

  private parseFecha(valor?: string): Date | null {
    if (!valor) {
      return null;
    }
    const d = new Date(valor);
    return Number.isNaN(d.getTime()) ? null : d;
  }
}
