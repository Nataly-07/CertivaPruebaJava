import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormsModule } from '@angular/forms';
import { AuthService } from '../../../Services/auth.service';
import { UsuarioService } from '../../../Services/usuario.service';
import { RolService } from '../../../Services/rol.service';
import { TipoDocumentoService } from '../../../Services/tipo-documento.service';
import { UsuarioDTO } from '../../../Models/usuario-dto';
import { RolDTO } from '../../../Models/rol-dto';
import { CrearUsuarioDTO } from '../../../Models/crear-usuario-dto';
import { TipoDocumentoDTO } from '../../../Models/tipo-documento-dto';
import { ImportacionCsvResultadoDTO } from '../../../Models/importacion-csv-dto';
import { AdminSidebarComponent } from '../../../Components/admin-sidebar/admin-sidebar.component';
import { etiquetaRol, normalizarCodigoRol } from '../../../constants/ui-labels';

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, AdminSidebarComponent],
  templateUrl: './usuarios.html',
  styleUrl: './usuarios.scss',
})
export class Usuarios implements OnInit {
  usuarios: UsuarioDTO[] = [];
  usuariosFiltrados: UsuarioDTO[] = [];
  roles: RolDTO[] = [];
  tiposDocumento: TipoDocumentoDTO[] = [];
  sidebarCollapsed = false;
  editForm!: FormGroup;
  createForm!: FormGroup;
  editingUser: UsuarioDTO | null = null;
  showModal = false;
  showCreateModal = false;
  showCsvModal = false;
  loading = false;
  importandoCsv = false;
  csvResultado: ImportacionCsvResultadoDTO | null = null;
  csvArchivo: File | null = null;

  filtroTexto = '';
  filtroRol: number | null = null;
  filtroEstado: 'todos' | 'activos' | 'inactivos' = 'todos';

  constructor(
    public authService: AuthService,
    private usuarioService: UsuarioService,
    private rolService: RolService,
    private tipoDocumentoService: TipoDocumentoService,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.initEditForm();
    this.initCreateForm();
    if (this.authService.isAdmin()) {
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

  get isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  get totalUsuarios(): number {
    return this.usuarios.length;
  }

  get usuariosActivos(): number {
    return this.usuarios.filter((u) => this.esUsuarioActivo(u)).length;
  }

  get usuariosInactivos(): number {
    return this.usuarios.length - this.usuariosActivos;
  }

  esUsuarioActivo(user: UsuarioDTO): boolean {
    const e = user.estado as unknown;
    return e === true || e === 'ACTIVO' || e === 'true';
  }

  etiquetaRolBadge(user: UsuarioDTO): string {
    const raw = user.rol?.nombre ?? user.rol?.codigo ?? '';
    return etiquetaRol(raw) || 'Sin rol';
  }

  etiquetaRolLista(rol: RolDTO): string {
    return etiquetaRol(rol.nombre || rol.codigo || '') || 'Sin rol';
  }

  claseBadgeRol(user: UsuarioDTO): string {
    const raw = (user.rol?.codigo ?? user.rol?.nombre ?? '').toLowerCase();
    const clave = normalizarCodigoRol(raw).toLowerCase().replace(/\s+/g, '_');
    return `role_${clave || 'desconocido'}`;
  }

  loadUsuarios(): void {
    this.loading = true;
    this.usuarioService.listar().subscribe({
      next: (data) => {
        this.usuarios = data;
        this.aplicarFiltros();
        this.loading = false;
      },
      error: (err) => {
        console.error('Error al cargar usuarios', err);
        this.loading = false;
      },
    });
  }

  aplicarFiltros(): void {
    const q = this.filtroTexto.trim().toLowerCase();
    this.usuariosFiltrados = this.usuarios.filter((u) => {
      if (this.filtroRol != null && u.rol?.idRol !== this.filtroRol && u.idRol !== this.filtroRol) {
        return false;
      }
      if (this.filtroEstado === 'activos' && !this.esUsuarioActivo(u)) return false;
      if (this.filtroEstado === 'inactivos' && this.esUsuarioActivo(u)) return false;
      if (!q) return true;
      const blob = `${u.nombres} ${u.apellidos} ${u.correo} ${u.numeroDocumento}`.toLowerCase();
      return blob.includes(q);
    });
  }

  private loadTiposDocumento(): void {
    this.tipoDocumentoService.listar().subscribe({
      next: (data) => (this.tiposDocumento = data),
      error: (err) => console.error('Error al cargar tipos de documento', err),
    });
  }

  private loadRoles(): void {
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

  openCsvModal(): void {
    this.csvArchivo = null;
    this.csvResultado = null;
    this.showCsvModal = true;
  }

  closeCsvModal(): void {
    this.showCsvModal = false;
  }

  onCsvSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.csvArchivo = input.files?.[0] ?? null;
  }

  importarCsv(): void {
    if (!this.csvArchivo) return;
    this.importandoCsv = true;
    this.usuarioService.importarCsv(this.csvArchivo).subscribe({
      next: (res) => {
        this.csvResultado = res;
        this.importandoCsv = false;
        this.loadUsuarios();
      },
      error: (err) => {
        console.error(err);
        this.importandoCsv = false;
        this.csvResultado = {
          filasExitosas: 0,
          filasConError: 1,
          erroresPorFila: [err?.error?.mensaje || 'Error al importar CSV'],
        };
      },
    });
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
      telefono: user.telefono ?? '',
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

    const nuevoRol = this.editForm.value.idRol;
    const rolAnterior = this.editingUser.rol?.idRol ?? this.editingUser.idRol;
    const rolCambio = this.isAdmin && nuevoRol != null && nuevoRol !== rolAnterior;

    const payload: Partial<UsuarioDTO> = {
      idUsuario: this.editingUser.idUsuario,
      nombres: this.editForm.value.nombres,
      apellidos: this.editForm.value.apellidos,
      correo: this.editForm.value.correo,
      numeroDocumento: this.editForm.value.numeroDocumento,
      telefono: this.editForm.value.telefono,
      estado: this.editingUser.estado as boolean,
      fechaRegistro: this.editingUser.fechaRegistro,
      idTipoDocumento: this.editForm.value.idTipoDocumento,
    };

    const guardarPerfil = () => {
      this.usuarioService.actualizar(this.editingUser!.idUsuario, payload).subscribe({
        next: () => {
          this.closeModal();
          this.loadUsuarios();
        },
        error: (err) => console.error('Error al actualizar usuario', err),
      });
    };

    if (rolCambio) {
      this.usuarioService.cambiarRol(this.editingUser.idUsuario, nuevoRol).subscribe({
        next: () => guardarPerfil(),
        error: (err) => console.error('Error al cambiar rol', err),
      });
    } else {
      guardarPerfil();
    }
  }

  inactivarUsuario(user: UsuarioDTO): void {
    if (!confirm(`¿Estás seguro de inactivar a ${user.nombres} ${user.apellidos}?`)) return;
    this.usuarioService.inactivar(user.idUsuario).subscribe({
      next: () => this.loadUsuarios(),
      error: (err) => console.error('Error al inactivar usuario', err),
    });
  }
}
