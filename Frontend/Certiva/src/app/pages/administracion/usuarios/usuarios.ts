import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../Services/auth.service';
import { UsuarioService } from '../../../Services/usuario.service';
import { RolService } from '../../../Services/rol.service';
import { TipoDocumentoService } from '../../../Services/tipo-documento.service';
import { UsuarioDTO } from '../../../Models/usuario-dto';
import { RolDTO } from '../../../Models/rol-dto';
import { CrearUsuarioDTO } from '../../../Models/crear-usuario-dto';
import { TipoDocumentoDTO } from '../../../Models/tipo-documento-dto';

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, RouterLinkActive],
  templateUrl: './usuarios.html',
  styleUrl: './usuarios.scss',
})
export class Usuarios implements OnInit {
  /** Traducción de códigos técnicos a etiquetas legibles en la tabla. */
  readonly mapaRoles: Record<string, string> = {
    ROLE_ADMIN: 'Administrador',
    ROLE_PROFESOR: 'Profesor',
    ROLE_MONITOR: 'Monitor',
    ROLE_ESTUDIANTE: 'Estudiante',
  };

  usuarios: UsuarioDTO[] = [];
  roles: RolDTO[] = [];
  tiposDocumento: TipoDocumentoDTO[] = [];
  sidebarCollapsed = false;
  editForm!: FormGroup;
  createForm!: FormGroup;
  editingUser: UsuarioDTO | null = null;
  showModal = false;
  showCreateModal = false;
  loading = false;

  constructor(
    public authService: AuthService,
    private usuarioService: UsuarioService,
    private rolService: RolService,
    private tipoDocumentoService: TipoDocumentoService,
    private fb: FormBuilder,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initEditForm();
    this.initCreateForm();
    if (this.authService.isStaff()) {
      this.loadUsuarios();
      this.loadTiposDocumento();
      this.loadRoles();
    }
  }

  private initEditForm(): void {
    this.editForm = this.fb.group({
      nombres: ['', Validators.required],
      apellidos: ['', Validators.required],
      correo: ['', [Validators.required, Validators.email]],
      telefono: [''],
      numeroDocumento: [''],
      idRol: [null],
      idTipoDocumento: [null],
      contrasena: [''],
    });
  }

  private initCreateForm(): void {
    this.createForm = this.fb.group({
      nombres: ['', Validators.required],
      apellidos: ['', Validators.required],
      correo: ['', [Validators.required, Validators.email]],
      numeroDocumento: ['', Validators.required],
      idTipoDocumento: [null, Validators.required],
      idRol: [null, Validators.required],
      contrasena: ['', [Validators.required, Validators.minLength(8)]],
    });
  }

  get usuario() {
    return this.authService.getUsuario();
  }

  get isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  get totalUsuarios(): number {
    return this.usuarios.length;
  }

  get usuariosActivos(): number {
    return this.usuarios.filter((u) => {
      const e = u.estado as unknown;
      return e === true || e === 'ACTIVO' || e === 'true';
    }).length;
  }

  get usuariosInactivos(): number {
    return this.usuarios.length - this.usuariosActivos;
  }

  esUsuarioActivo(user: UsuarioDTO): boolean {
    const e = user.estado as unknown;
    return e === true || e === 'ACTIVO' || e === 'true';
  }

  etiquetaRolBadge(user: UsuarioDTO): string {
    const codigo = (user.rol?.codigo ?? '').toUpperCase();
    if (codigo && this.mapaRoles[codigo]) {
      return this.mapaRoles[codigo];
    }
    const nombre = (user.rol?.nombre ?? '').toUpperCase();
    if (nombre && this.mapaRoles[nombre]) {
      return this.mapaRoles[nombre];
    }
    const raw = user.rol?.nombre ?? user.rol?.codigo ?? '';
    const limpio = raw.replace(/^ROLE_/i, '').replace(/_/g, ' ').trim().toLowerCase();
    if (!limpio) {
      return 'Sin rol';
    }
    return limpio.charAt(0).toUpperCase() + limpio.slice(1);
  }

  claseBadgeRol(user: UsuarioDTO): string {
    const raw = (user.rol?.codigo ?? user.rol?.nombre ?? '').toLowerCase();
    const clave = raw.replace(/^role_/, '').replace(/\s+/g, '_');
    return `role_${clave || 'desconocido'}`;
  }

  toggleSidebar(): void {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }

  loadUsuarios(): void {
    this.loading = true;
    this.usuarioService.listar().subscribe({
      next: (data) => {
        this.usuarios = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error al cargar usuarios', err);
        this.loading = false;
      },
    });
  }

  private loadTiposDocumento(): void {
    this.tipoDocumentoService.listar().subscribe({
      next: (data) => (this.tiposDocumento = data),
      error: (err) => console.error('Error al cargar tipos de documento', err),
    });
  }

  private loadRoles(): void {
    if (!this.authService.isAdmin()) {
      this.roles = [];
      return;
    }
    this.rolService.listarParaAdministracion().subscribe({
      next: (data) => (this.roles = data),
      error: (err) => console.error('Error al cargar roles', err),
    });
  }

  openCreateModal(): void {
    this.createForm.reset();
    this.showCreateModal = true;
  }

  closeCreateModal(): void {
    this.showCreateModal = false;
  }

  saveCreate(): void {
    if (this.createForm.invalid || !this.isAdmin) return;

    const v = this.createForm.value;
    const dto: CrearUsuarioDTO = {
      nombres: v.nombres,
      apellidos: v.apellidos,
      correo: v.correo,
      contrasena: v.contrasena,
      telefono: '',
      numeroDocumento: v.numeroDocumento,
      idRol: v.idRol,
      idTipoDocumento: v.idTipoDocumento,
    };

    this.usuarioService.crearDesdeAdministracion(dto).subscribe({
      next: () => {
        this.closeCreateModal();
        this.loadUsuarios();
      },
      error: (err) => console.error('Error al crear usuario', err),
    });
  }

  openEditModal(user: UsuarioDTO): void {
    this.editingUser = user;
    this.editForm.patchValue({
      nombres: user.nombres,
      apellidos: user.apellidos,
      correo: user.correo,
      numeroDocumento: user.numeroDocumento,
      idRol: user.rol?.idRol ?? user.idRol,
      idTipoDocumento: user.tipoDocumento?.idTipoDocumento ?? user.idTipoDocumento,
      telefono: '',
      contrasena: '',
    });
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.editingUser = null;
  }

  saveEdit(): void {
    if (this.editForm.invalid || !this.editingUser) return;

    const payload: Partial<UsuarioDTO> = {
      idUsuario: this.editingUser.idUsuario,
      nombres: this.editForm.value.nombres,
      apellidos: this.editForm.value.apellidos,
      correo: this.editForm.value.correo,
      numeroDocumento: this.editForm.value.numeroDocumento,
      estado: this.editingUser.estado as boolean,
      fechaRegistro: this.editingUser.fechaRegistro,
      idTipoDocumento: this.editForm.value.idTipoDocumento,
    };

    if (this.isAdmin) {
      payload.idRol = this.editForm.value.idRol;
    }

    this.usuarioService.actualizar(this.editingUser.idUsuario, payload).subscribe({
      next: () => {
        this.closeModal();
        this.loadUsuarios();
      },
      error: (err) => console.error('Error al actualizar usuario', err),
    });
  }

  inactivarUsuario(user: UsuarioDTO): void {
    if (!confirm(`¿Estás seguro de inactivar a ${user.nombres} ${user.apellidos}?`)) return;

    this.usuarioService.inactivar(user.idUsuario).subscribe({
      next: () => this.loadUsuarios(),
      error: (err) => console.error('Error al inactivar usuario', err),
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
