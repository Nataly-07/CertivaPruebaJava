import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../Services/auth.service';
import { CertificadoService } from '../../../Services/certificado.service';
import { CertificadoDTO } from '../../../Models/certificado-dto';

@Component({
  selector: 'app-certificados',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './certificados.html',
  styleUrl: './certificados.scss',
})
export class Certificados implements OnInit {
  certificados: CertificadoDTO[] = [];
  loading = false;
  errorMessage = '';

  constructor(
    public authService: AuthService,
    private certificadoService: CertificadoService
  ) {}

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.loading = true;
    this.errorMessage = '';
    this.certificadoService.listar().subscribe({
      next: (data) => {
        this.certificados = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'No se pudieron cargar los certificados.';
      },
    });
  }
}
