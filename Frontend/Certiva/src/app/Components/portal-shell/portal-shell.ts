import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../Services/auth.service';

@Component({
  selector: 'app-portal-shell',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './portal-shell.html',
  styleUrl: './portal-shell.scss',
})
export class PortalShell {
  constructor(
    private auth: AuthService,
    private router: Router
  ) {}

  get iniciales(): string {
    const u = this.auth.getUsuario();
    if (!u) {
      return '?';
    }
    const a = (u.nombres?.trim()[0] ?? '') + (u.apellidos?.trim()[0] ?? '');
    return a.toUpperCase() || '?';
  }

  get nombreCorto(): string {
    const u = this.auth.getUsuario();
    const n = u?.nombres?.trim();
    return n ? n.split(/\s+/)[0] : 'Estudiante';
  }

  get correoUsuario(): string {
    return this.auth.getUsuario()?.correo ?? '';
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
