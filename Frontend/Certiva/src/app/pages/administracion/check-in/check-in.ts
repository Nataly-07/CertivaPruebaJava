import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../Services/auth.service';
import { InscripcionService } from '../../../Services/inscripcion.service';
import { CheckInRespuestaDTO } from '../../../Models/certificado-dto';

@Component({
  selector: 'app-check-in',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './check-in.html',
  styleUrl: './check-in.scss',
})
export class CheckIn {
  codigoQr = '';
  loading = false;
  errorMessage = '';
  resultado: CheckInRespuestaDTO | null = null;

  constructor(
    public authService: AuthService,
    private inscripcionService: InscripcionService
  ) {}

  get usuario() {
    return this.authService.getUsuario();
  }

  confirmar(): void {
    const codigo = this.codigoQr.trim();
    if (!codigo) {
      this.errorMessage = 'Ingresa el código QR de la inscripción.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.resultado = null;

    this.inscripcionService.checkIn(codigo).subscribe({
      next: (res) => {
        this.loading = false;
        this.resultado = res;
        this.codigoQr = '';
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage =
          err.error?.mensaje || err.error?.message || 'No se pudo registrar el check-in.';
      },
    });
  }
}
