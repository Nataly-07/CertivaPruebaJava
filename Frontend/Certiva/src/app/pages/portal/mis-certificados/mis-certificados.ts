import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CertificadoService } from '../../../Services/certificado.service';
import { CertificadoPortalDTO } from '../../../Models/portal-dto';

@Component({
  selector: 'app-mis-certificados',
  standalone: true,
  imports: [CommonModule, DatePipe, RouterLink],
  templateUrl: './mis-certificados.html',
  styleUrl: './mis-certificados.scss',
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
