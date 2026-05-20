import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { LoginDTO } from '../Models/login-dto';
import { LoginRespuestaDTO } from '../Models/login-respuesta-dto';
import { UsuarioDTO } from '../Models/usuario-dto';

/** Roles de personal (administración y operación). */
export const STAFF_ROLES = ['ADMIN', 'PROFESOR', 'MONITOR'] as const;

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private apiUrl = `${environment.API_URL}usuarios`;

  constructor(private http: HttpClient) {}

  login(loginDTO: LoginDTO): Observable<LoginRespuestaDTO> {
    return this.http.post<LoginRespuestaDTO>(`${this.apiUrl}/login`, loginDTO).pipe(
      tap((res) => {
        localStorage.setItem('token', res.token);
        localStorage.setItem('usuario', JSON.stringify(res.usuarioDTO));
      })
    );
  }

  isLoggedIn(): boolean {
    const token = this.getToken();
    if (!token) return false;
    return !this.isTokenExpired(token);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  getUsuario(): UsuarioDTO | null {
    const data = localStorage.getItem('usuario');
    return data ? JSON.parse(data) : null;
  }

  getRol(): string | null {
    const token = this.getToken();
    if (!token) return null;
    const payload = this.decodeToken(token);
    if (!payload?.rol) return null;
    return payload.rol.replace(/^ROLE_/, '').toUpperCase();
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('usuario');
  }

  isAdmin(): boolean {
    return this.getRol() === 'ADMIN';
  }

  isProfesor(): boolean {
    return this.getRol() === 'PROFESOR';
  }

  isMonitor(): boolean {
    return this.getRol() === 'MONITOR';
  }

  isEstudiante(): boolean {
    return this.getRol() === 'ESTUDIANTE';
  }

  /** Admin, profesor o monitor (acceso al panel de administración). */
  isStaff(): boolean {
    return this.hasRole(...STAFF_ROLES);
  }

  hasRole(...roles: string[]): boolean {
    const userRol = this.getRol();
    if (!userRol) return false;
    return roles.map((r) => r.toUpperCase()).includes(userRol);
  }

  private decodeToken(token: string): { rol?: string; exp?: number } | null {
    try {
      const payload = token.split('.')[1];
      const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
      return JSON.parse(decoded);
    } catch {
      return null;
    }
  }

  private isTokenExpired(token: string): boolean {
    const payload = this.decodeToken(token);
    if (!payload?.exp) return true;
    return Date.now() >= payload.exp * 1000;
  }
}
