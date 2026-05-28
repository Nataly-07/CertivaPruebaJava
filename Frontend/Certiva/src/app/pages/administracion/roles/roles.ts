import { Component, OnInit } from '@angular/core';
import { RolDTO } from '../../../Models/rol-dto';
import { CrearRolDTO } from '../../../Models/crear-rol-dto';
import { RolService } from '../../../Services/rol.service';
import { AuthService } from '../../../Services/auth.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { etiquetaRol } from '../../../constants/ui-labels';

@Component({
  selector: 'app-roles',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './roles.html',
  styleUrl: './roles.scss',
})
export class Roles implements OnInit {

  roles: RolDTO[] = [];
  loading = false;
  errorMessage = '';
  successMessage = '';

  nuevoRol: CrearRolDTO = {
    nombre: '',
    descripcion: ''
  };

  editingRol: RolDTO | null = null;
  editRol: CrearRolDTO = { nombre: '', descripcion: '' };

  constructor(
    private rolService: RolService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.listarRoles();
  }

  get isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  listarRoles(): void {
    this.loading = true;
    this.rolService.listarTodos().subscribe({
      next: (data) => {
        this.roles = data;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = 'Error al cargar roles';
        this.loading = false;
      }
    });
  }

  crearRol(): void {
    if (!this.nuevoRol.nombre || !this.nuevoRol.descripcion) {
      this.errorMessage = 'Todos los campos son obligatorios';
      return;
    }

    this.errorMessage = '';
    this.rolService.crearRol(this.nuevoRol).subscribe({
      next: () => {
        this.nuevoRol = { nombre: '', descripcion: '' };
        this.successMessage = 'Rol creado exitosamente';
        this.listarRoles();
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (err) => {
        this.errorMessage = err.error?.mensaje || 'Error al crear rol';
      }
    });
  }

  openEdit(rol: RolDTO): void {
    this.editingRol = rol;
    this.editRol = {
      nombre: this.displayRolName(rol.nombre),
      descripcion: rol.descripcion ?? ''
    };
  }

  cancelEdit(): void {
    this.editingRol = null;
  }

  saveEdit(): void {
    if (!this.editingRol) return;

    this.rolService.actualizarRol(this.editingRol.idRol, this.editRol).subscribe({
      next: () => {
        this.editingRol = null;
        this.successMessage = 'Rol actualizado exitosamente';
        this.listarRoles();
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (err) => {
        this.errorMessage = err.error?.mensaje || 'Error al actualizar rol';
      }
    });
  }

  inactivarRol(rol: RolDTO): void {
    if (!confirm(`¿Estás seguro de inactivar el rol "${this.displayRolName(rol.nombre)}"?`)) return;

    this.rolService.inactivarRol(rol.idRol).subscribe({
      next: () => {
        this.successMessage = 'Rol inactivado correctamente';
        this.listarRoles();
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (err) => {
        this.errorMessage = err.error?.mensaje || 'Error al inactivar rol';
      }
    });
  }

  displayRolName(nombre: string): string {
    return etiquetaRol(nombre);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
