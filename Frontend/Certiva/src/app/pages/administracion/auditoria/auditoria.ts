import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuditoriaService } from '../../../Services/auditoria.service';
import { AuditoriaResumenDTO } from '../../../Models/auditoria-dto';

@Component({
  selector: 'app-auditoria',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './auditoria.html',
  styleUrl: './auditoria.scss',
})
export class AuditoriaPage implements OnInit {
  registros: AuditoriaResumenDTO[] = [];
  loading = false;
  errorMessage = '';

  constructor(private auditoriaService: AuditoriaService) {}

  ngOnInit(): void {
    this.loading = true;
    this.auditoriaService.listarRecientes(100).subscribe({
      next: (data) => {
        this.registros = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'No se pudo cargar la auditoría.';
      },
    });
  }
}
