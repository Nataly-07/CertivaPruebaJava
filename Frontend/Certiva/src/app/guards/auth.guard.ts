import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../Services/auth.service';

function redirigirSegunRol(authService: AuthService, router: Router): void {
  if (!authService.isLoggedIn()) {
    router.navigate(['/login']);
    return;
  }
  if (authService.isAdmin()) {
    router.navigate(['/admin/dashboard']);
  } else if (authService.isProfesor()) {
    router.navigate(['/admin/mi-panel']);
  } else if (authService.isMonitor()) {
    router.navigate(['/admin/check-in']);
  } else {
    router.navigate(['/portal']);
  }
}

export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn()) {
    return true;
  }

  router.navigate(['/login']);
  return false;
};

export const adminGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn() && authService.isAdmin()) {
    return true;
  }

  redirigirSegunRol(authService, router);
  return false;
};

export const profesorGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn() && (authService.isProfesor() || authService.isAdmin())) {
    return true;
  }

  redirigirSegunRol(authService, router);
  return false;
};

export const monitorGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn() && (authService.isMonitor() || authService.isAdmin())) {
    return true;
  }

  redirigirSegunRol(authService, router);
  return false;
};

/** Admin o profesor: gestión de eventos. */
export const staffGuard: CanActivateFn = profesorGuard;

export const estudianteGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn() && authService.isEstudiante()) {
    return true;
  }

  redirigirSegunRol(authService, router);
  return false;
};

export const checkInGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn() && authService.hasRole('ADMIN', 'MONITOR')) {
    return true;
  }

  redirigirSegunRol(authService, router);
  return false;
};
