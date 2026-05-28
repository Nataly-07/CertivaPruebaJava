import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../Services/auth.service';
import { etiquetaRol } from '../../constants/ui-labels';

@Component({
  selector: 'app-admin-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './admin-sidebar.component.html',
  styleUrl: './admin-sidebar.component.scss',
})
export class AdminSidebarComponent {
  @Input() collapsed = false;
  @Output() collapsedChange = new EventEmitter<boolean>();

  constructor(
    public authService: AuthService,
    private router: Router
  ) {}

  get usuario() {
    return this.authService.getUsuario();
  }

  get nombreRolUsuario(): string {
    return etiquetaRol(this.usuario?.rol?.nombre ?? this.usuario?.rol?.codigo ?? '');
  }

  toggle(): void {
    this.collapsedChange.emit(!this.collapsed);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
