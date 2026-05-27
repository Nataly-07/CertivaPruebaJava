import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../Services/auth.service';

@Component({
  selector: 'app-profesor-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './profesor-sidebar.component.html',
  styleUrl: './profesor-sidebar.component.scss',
})
export class ProfesorSidebarComponent {
  @Input() collapsed = false;
  @Output() collapsedChange = new EventEmitter<boolean>();

  constructor(
    public authService: AuthService,
    private router: Router
  ) {}

  get usuario() {
    return this.authService.getUsuario();
  }

  toggle(): void {
    this.collapsedChange.emit(!this.collapsed);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
