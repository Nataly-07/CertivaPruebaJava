import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CertificadoService } from '../../../Services/certificado.service';
import { CertificadoVerificacionDTO } from '../../../Models/certificado-dto';

@Component({
  selector: 'app-verificar-certificado',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './verificar-certificado.html',
  styleUrl: './verificar-certificado.scss',
})
export class VerificarCertificado {
  codigo = '';
  loading = false;
  errorMessage = '';
  resultado: CertificadoVerificacionDTO | null = null;

  constructor(private certificadoService: CertificadoService) {}

  verificar(): void {
    const codigo = this.codigo.trim();
    if (!codigo) {
      this.errorMessage = 'Ingresa el código de verificación del certificado.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.resultado = null;

    this.certificadoService.verificarPublico(codigo).subscribe({
      next: (res) => {
        this.loading = false;
        this.resultado = res;
        if (!res.valido) {
          this.errorMessage = res.mensaje;
        }
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'No se pudo verificar el certificado. Intenta de nuevo.';
      },
    });
  }
}
