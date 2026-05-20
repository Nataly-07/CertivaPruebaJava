import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { UsuarioService } from '../../../Services/usuario.service';
import { TipoDocumentoService } from '../../../Services/tipo-documento.service';
import { RolService } from '../../../Services/rol.service';
import { TipoDocumentoDTO } from '../../../Models/tipo-documento-dto';
import { RolDTO } from '../../../Models/rol-dto';
import { CrearUsuarioDTO } from '../../../Models/crear-usuario-dto';

@Component({
  selector: 'app-registro',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './registro.html',
  styleUrl: './registro.scss',
})
export class Registro implements OnInit {
  registroForm!: FormGroup;
  tiposDocumento: TipoDocumentoDTO[] = [];
  roles: RolDTO[] = [];
  errorMessage = '';
  successMessage = '';
  loading = false;
  showPassword = false;
  showConfirmPassword = false;

  constructor(
    private fb: FormBuilder,
    private usuarioService: UsuarioService,
    private tipoDocService: TipoDocumentoService,
    private rolService: RolService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadTiposDocumento();
    this.loadRoles();
  }

  private initForm(): void {
    this.registroForm = this.fb.group({
      nombres: ['', [Validators.required]],
      apellidos: ['', [Validators.required]],
      correo: ['', [Validators.required, Validators.email]],
      telefono: [''],
      contrasena: ['', [Validators.required, Validators.minLength(8)]],
      confirmarContrasena: ['', [Validators.required]],
      numeroDocumento: ['', [Validators.pattern(/^\d+$/)]],
      idTipoDocumento: [null],
      idRol: [null],
    }, { validators: this.passwordMatchValidator });
  }

  private passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('contrasena');
    const confirm = control.get('confirmarContrasena');
    if (password && confirm && password.value !== confirm.value) {
      confirm.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    }
    if (confirm?.hasError('passwordMismatch')) {
      confirm.setErrors(null);
    }
    return null;
  }

  get f() {
    return this.registroForm.controls;
  }

  get passwordStrength(): string {
    const val = this.f['contrasena'].value || '';
    if (val.length < 8) return 'weak';
    const hasUpper = /[A-Z]/.test(val);
    const hasLower = /[a-z]/.test(val);
    const hasNumber = /\d/.test(val);
    const hasSpecial = /[!@#$%^&*(),.?":{}|<>]/.test(val);
    const score = [hasUpper, hasLower, hasNumber, hasSpecial].filter(Boolean).length;
    if (score >= 3 && val.length >= 10) return 'strong';
    if (score >= 2) return 'medium';
    return 'weak';
  }

  get passwordsMatch(): boolean {
    return (
      this.f['contrasena'].value &&
      this.f['confirmarContrasena'].value &&
      this.f['contrasena'].value === this.f['confirmarContrasena'].value
    );
  }

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  toggleConfirmPassword(): void {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  private loadTiposDocumento(): void {
    this.tipoDocService.listar().subscribe({
      next: (data) => (this.tiposDocumento = data),
      error: (err) => console.error('Error cargando tipos de documento', err),
    });
  }

  private loadRoles(): void {
    this.rolService.listarRolesRegistro().subscribe({
      next: (data) => {
        this.roles = data;
        const estudianteRol = data.find(
          (r) => r.nombre.toUpperCase().includes('ESTUDIANTE')
        );
        if (estudianteRol) {
          this.registroForm.patchValue({ idRol: estudianteRol.idRol });
        }
      },
      error: (err) => console.error('Error cargando roles', err),
    });
  }

  onSubmit(): void {
    if (this.registroForm.invalid) {
      this.registroForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const dto: CrearUsuarioDTO = {
      nombres: this.registroForm.value.nombres,
      apellidos: this.registroForm.value.apellidos,
      correo: this.registroForm.value.correo,
      contrasena: this.registroForm.value.contrasena,
      telefono: this.registroForm.value.telefono || '',
      numeroDocumento: this.registroForm.value.numeroDocumento || '',
      idRol: this.registroForm.value.idRol,
      idTipoDocumento: this.registroForm.value.idTipoDocumento,
    };

    this.usuarioService.registrar(dto).subscribe({
      next: () => {
        this.loading = false;
        this.successMessage = 'Cuenta creada exitosamente. Redirigiendo al login...';
        setTimeout(() => this.router.navigate(['/login']), 2000);
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.mensaje || err.error?.message || 'Error al crear la cuenta. Intenta de nuevo.';
      },
    });
  }
}
