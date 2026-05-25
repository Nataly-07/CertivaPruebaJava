import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../Services/auth.service';
import { InscripcionService } from '../../../Services/inscripcion.service';
import { CheckInRespuestaDTO } from '../../../Models/certificado-dto';
import { normalizarCodigoQr } from '../../../utils/inscripcion-qr';

@Component({
  selector: 'app-check-in',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './check-in.html',
  styleUrl: './check-in.scss',
})
export class CheckIn implements OnInit, OnDestroy {
  codigoQr = '';
  tipoAsistencia: 'PRESENTE' | 'TARDIO' = 'PRESENTE';
  loading = false;
  errorMessage = '';
  resultado: CheckInRespuestaDTO | null = null;

  escaneando = false;
  camaraError = '';
  private stream: MediaStream | null = null;
  private scanTimer: ReturnType<typeof setInterval> | null = null;

  constructor(
    public authService: AuthService,
    private inscripcionService: InscripcionService
  ) {}

  get usuario() {
    return this.authService.getUsuario();
  }

  ngOnInit(): void {
    if (this.authService.isMonitor() && !this.authService.isAdmin()) {
      // Ruta exclusiva monitor según HU
    }
  }

  ngOnDestroy(): void {
    this.detenerCamara();
  }

  async iniciarCamara(): Promise<void> {
    this.camaraError = '';
    this.detenerCamara();
    if (!navigator.mediaDevices?.getUserMedia) {
      this.camaraError = 'Su navegador no soporta acceso a la cámara. Use el campo manual.';
      return;
    }
    try {
      this.stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'environment' },
        audio: false,
      });
      const video = document.getElementById('checkin-video') as HTMLVideoElement | null;
      if (video) {
        video.srcObject = this.stream;
        await video.play();
      }
      this.escaneando = true;
      this.iniciarDeteccionNativa();
    } catch {
      this.camaraError = 'No se pudo activar la cámara. Revise permisos o use entrada manual.';
      this.detenerCamara();
    }
  }

  detenerCamara(): void {
    this.escaneando = false;
    if (this.scanTimer) {
      clearInterval(this.scanTimer);
      this.scanTimer = null;
    }
    if (this.stream) {
      this.stream.getTracks().forEach(t => t.stop());
      this.stream = null;
    }
    const video = document.getElementById('checkin-video') as HTMLVideoElement | null;
    if (video) {
      video.srcObject = null;
    }
  }

  private iniciarDeteccionNativa(): void {
    const Detector = (window as unknown as { BarcodeDetector?: new (opts: { formats: string[] }) => {
      detect: (source: HTMLVideoElement) => Promise<{ rawValue: string }[]>;
    } }).BarcodeDetector;
    if (!Detector) {
      this.camaraError = 'Escaneo automático no disponible; ingrese el código manualmente.';
      return;
    }
    const detector = new Detector({ formats: ['qr_code'] });
    const video = document.getElementById('checkin-video') as HTMLVideoElement | null;
    if (!video) return;

    this.scanTimer = setInterval(async () => {
      if (!this.escaneando || !video.videoWidth) return;
      try {
        const codes = await detector.detect(video);
        const raw = codes[0]?.rawValue;
        if (raw) {
          this.codigoQr = normalizarCodigoQr(raw);
          this.detenerCamara();
          this.confirmar();
        }
      } catch {
        /* frame sin lectura */
      }
    }, 500);
  }

  confirmar(): void {
    const codigo = this.codigoQr.trim();
    if (!codigo) {
      this.errorMessage = 'Ingresa o escanea el código QR de la inscripción.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.resultado = null;

    this.inscripcionService.checkIn(codigo, this.tipoAsistencia).subscribe({
      next: res => {
        this.loading = false;
        this.resultado = res;
        this.codigoQr = '';
      },
      error: err => {
        this.loading = false;
        this.errorMessage =
          err.error?.mensaje || err.error?.message || 'No se pudo registrar el check-in.';
      },
    });
  }
}
