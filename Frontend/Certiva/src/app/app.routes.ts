import { Routes } from '@angular/router';
import {
  authGuard,
  adminGuard,
  profesorGuard,
  monitorGuard,
  checkInGuard,
  estudianteGuard,
} from './guards/auth.guard';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./pages/auth/login/login').then(m => m.Login) },
  { path: 'registro', loadComponent: () => import('./pages/auth/registro/registro').then(m => m.Registro) },
  {
    path: 'verificar-certificado',
    loadComponent: () =>
      import('./pages/public/verificar-certificado/verificar-certificado').then(m => m.VerificarCertificado),
  },
  {
    path: 'catalogo',
    loadComponent: () =>
      import('./pages/portal/eventos-disponibles/eventos-disponibles').then(m => m.EventosDisponibles),
  },
  {
    path: 'portal/inscribir/d/:codigo',
    loadComponent: () => import('./pages/portal/inscribir-evento/inscribir-evento').then(m => m.InscribirEvento),
  },
  {
    path: 'portal',
    loadComponent: () => import('./Components/portal-shell/portal-shell').then(m => m.PortalShell),
    canActivate: [authGuard, estudianteGuard],
    children: [
      { path: '', loadComponent: () => import('./pages/portal/portal').then(m => m.Portal) },
      {
        path: 'eventos',
        loadComponent: () =>
          import('./pages/portal/eventos-disponibles/eventos-disponibles').then(m => m.EventosDisponibles),
      },
      {
        path: 'mis-eventos',
        loadComponent: () => import('./pages/portal/mis-eventos/mis-eventos').then(m => m.MisEventos),
      },
      {
        path: 'certificados',
        loadComponent: () => import('./pages/portal/mis-certificados/mis-certificados').then(m => m.MisCertificados),
      },
      {
        path: 'eventos/:id/inscribir',
        loadComponent: () => import('./pages/portal/inscribir-evento/inscribir-evento').then(m => m.InscribirEvento),
      },
    ],
  },
  {
    path: 'admin/dashboard',
    loadComponent: () => import('./pages/administracion/dashboard/dashboard').then(m => m.Dashboard),
    canActivate: [authGuard, adminGuard],
  },
  {
    path: 'admin/auditoria',
    loadComponent: () => import('./pages/administracion/auditoria/auditoria').then(m => m.AuditoriaPage),
    canActivate: [authGuard, adminGuard],
  },
  {
    path: 'admin/mi-panel',
    loadComponent: () => import('./pages/administracion/mi-panel/mi-panel').then(m => m.MiPanel),
    canActivate: [authGuard, profesorGuard],
  },
  {
    path: 'admin/eventos',
    loadComponent: () => import('./pages/administracion/eventos/eventos').then(m => m.Eventos),
    canActivate: [authGuard, adminGuard],
  },
  {
    path: 'admin/usuarios',
    loadComponent: () => import('./pages/administracion/usuarios/usuarios').then(m => m.Usuarios),
    canActivate: [authGuard, adminGuard],
  },
  {
    path: 'admin/check-in',
    loadComponent: () => import('./pages/administracion/check-in/check-in').then(m => m.CheckIn),
    canActivate: [authGuard, checkInGuard],
  },
  {
    path: 'monitor/checkin',
    redirectTo: '/admin/check-in',
    pathMatch: 'full',
  },
  {
    path: 'admin/certificados',
    loadComponent: () => import('./pages/administracion/certificados/certificados').then(m => m.Certificados),
    canActivate: [authGuard, adminGuard],
  },
  { path: 'admin/roles', redirectTo: '/admin/usuarios', pathMatch: 'full' },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' },
];

