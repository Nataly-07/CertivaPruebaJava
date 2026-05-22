import { Component, OnInit, ViewChild, inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { EventoService } from '../../../Services/evento.service';
import { InscripcionService } from '../../../Services/inscripcion.service';
import { AuthService } from '../../../Services/auth.service';
import { UsuarioService } from '../../../Services/usuario.service';
import { EventoDTO, CampoFormularioDTO } from '../../../Models/evento-dto';
import { QrCodeDisplayComponent } from '../../../Components/qr-code-display/qr-code-display.component';
import { DynamicEventFieldsComponent } from '../../../Components/dynamic-event-fields/dynamic-event-fields.component';
import { tap } from 'rxjs';

@Component({
  selector: 'app-inscribir-evento',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    DatePipe,
    ReactiveFormsModule,
    DynamicEventFieldsComponent,
    QrCodeDisplayComponent,
  ],
  template: `
    <div class="portal-wrap">
      <div class="portal-inner glass-card">
        @if (!auth.isLoggedIn()) {
          <div class="login-prompt">
            <h1 class="page-title">Inscripción al evento</h1>
            <p class="text-secondary mb-3">
              Para inscribirte necesitas una cuenta en Certiva. Inicia sesión y volverás automáticamente a este evento.
            </p>
            <button type="button" class="btn btn-gradient" (click)="irALogin()">Iniciar sesión</button>
            <a routerLink="/registro" class="btn btn-dark-outline ms-2">Crear cuenta</a>
          </div>
        } @else if (loading) {
          <p class="text-secondary">Cargando evento…</p>
        } @else if (evento) {
          <a routerLink="/portal/eventos" class="back-link">← Eventos disponibles</a>

          @if (errorMsg) {
            <div class="alert alert-warning py-2 px-3 mb-3">{{ errorMsg }}</div>
          }

          <h1 class="page-title mb-2">{{ evento.nombreEvento }}</h1>
          <p class="text-secondary small mb-1">
            <strong>Inicio:</strong> {{ evento.fechaInicio | date: 'short' }} — <strong>Fin:</strong>
            {{ evento.fechaFin | date: 'short' }}
          </p>

          @if (!hayCupo) {
            <div class="alert alert-danger py-2 px-3 mb-3">No hay cupo disponible para este evento.</div>
          }

          <section class="datos-usuario mb-4 p-3 rounded">
            <h2 class="h6 text-white-50 mb-2">Tus datos</h2>
            <p class="small mb-1"><strong>Nombre:</strong> {{ nombreCompleto }}</p>
            <p class="small mb-2"><strong>Correo:</strong> {{ correoUsuario }}</p>
            @if (faltaTelefono) {
              <label class="form-label small">Teléfono de contacto (obligatorio)</label>
              <input
                type="tel"
                class="form-control form-control-sm mb-2"
                [formControl]="telefonoCtrl"
                placeholder="Ej. 3001234567"
              />
              <button
                type="button"
                class="btn btn-dark-outline btn-sm"
                [disabled]="telefonoCtrl.invalid || guardandoTelefono"
                (click)="guardarTelefono()"
              >
                Guardar teléfono
              </button>
            }
          </section>

          <h2 class="h6 text-white-50 mb-3">Información adicional del evento</h2>
          <app-dynamic-event-fields [campos]="campos" #dynFields />

          <div class="d-flex gap-2 mt-4 flex-wrap">
            <button
              type="button"
              class="btn btn-gradient"
              [disabled]="!hayCupo || submitting || faltaTelefono"
              (click)="enviar()"
            >
              @if (submitting) {
                <span>Enviando…</span>
              } @else {
                <span>Confirmar inscripción</span>
              }
            </button>
          </div>

          @if (exitoMsg) {
            <div class="alert alert-success mt-3 py-2 px-3">{{ exitoMsg }}</div>
          }
          @if (tokenQrRegistro) {
            <div class="qr-inscripcion mt-3 p-3 rounded border border-secondary border-opacity-25">
              <h3 class="h6 text-white-50">QR de registro (pase de entrada)</h3>
              <p class="small text-muted mb-2">
                Guárdalo en tu celular y preséntalo en la entrada. El personal de apoyo lo escaneará para registrar tu asistencia.
              </p>
              <app-qr-code-display
                [data]="tokenQrRegistro"
                [size]="200"
                caption="Pase de entrada"
                [showUrl]="true"
              />
              <a routerLink="/portal/mis-eventos" class="btn btn-dark-outline btn-sm mt-3">Ver en Mis eventos</a>
            </div>
          }
        }
      </div>
    </div>
  `,
  styles: [
    `
      .portal-wrap {
        min-height: 100vh;
        padding: 2rem 1.25rem;
        background: var(--gradient-bg);
      }
      .portal-inner {
        max-width: 640px;
        margin: 0 auto;
        padding: 2rem 1.75rem;
      }
      .datos-usuario {
        border: 1px solid rgba(34, 211, 238, 0.2);
        background: rgba(34, 211, 238, 0.05);
      }
      .login-prompt {
        text-align: center;
        padding: 1rem 0;
      }
      .back-link {
        display: inline-block;
        margin-bottom: 1rem;
        color: var(--accent-cyan);
        text-decoration: none;
        font-size: 0.9rem;
      }
      .page-title {
        font-size: 1.45rem;
        font-weight: 800;
        color: var(--text-primary);
      }
    `,
  ],
})
export class InscribirEvento implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private eventoService = inject(EventoService);
  private inscripcionService = inject(InscripcionService);
  private usuarioService = inject(UsuarioService);
  auth = inject(AuthService);
  private fb = inject(FormBuilder);

  @ViewChild('dynFields') dynFields?: DynamicEventFieldsComponent;

  evento: EventoDTO | null = null;
  campos: CampoFormularioDTO[] = [];
  tokenQrRegistro: string | null = null;

  loading = true;
  submitting = false;
  guardandoTelefono = false;
  errorMsg: string | null = null;
  exitoMsg: string | null = null;
  hayCupo = true;

  nombreCompleto = '';
  correoUsuario = '';
  faltaTelefono = false;

  telefonoCtrl = this.fb.control('', [Validators.required, Validators.minLength(7), Validators.maxLength(20)]);

  ngOnInit(): void {
    if (!this.auth.isLoggedIn()) {
      this.loading = false;
      return;
    }
    this.cargarPerfil();
    const codigoDifusion = this.route.snapshot.paramMap.get('codigo');
    const idParam = this.route.snapshot.paramMap.get('id');

    if (codigoDifusion) {
      this.eventoService.obtenerPublicoPorCodigoDifusion(codigoDifusion).subscribe({
        next: pub => {
          this.evento = {
            idEvento: pub.idEvento,
            nombreEvento: pub.nombreEvento,
            descripcion: pub.descripcion,
            tipoEvento: pub.tipoEvento,
            modalidad: pub.modalidad,
            fechaInicio: pub.fechaInicio,
            fechaFin: pub.fechaFin,
            ubicacion: pub.ubicacion,
            enlaceVirtual: pub.enlaceVirtual,
            aforoMaximo: pub.aforoMaximo,
            intensidadHoraria: pub.intensidadHoraria,
            precio: pub.precio,
            gratuito: pub.gratuito,
            camposPersonalizados: pub.camposPersonalizados,
          };
          this.campos = pub.camposPersonalizados ?? [];
          this.hayCupo = pub.hayCupoDisponible;
          this.loading = false;
        },
        error: () => {
          this.loading = false;
          this.errorMsg = 'No se encontró el evento o el enlace de difusión no es válido.';
        },
      });
      return;
    }

    const id = Number(idParam);
    if (!Number.isFinite(id)) {
      this.router.navigate(['/portal/eventos']);
      return;
    }
    this.eventoService.obtener(id).subscribe({
      next: ev => {
        this.evento = ev;
        this.campos = ev.camposPersonalizados ?? [];
        this.loading = false;
        this.refrescarCupo(id);
      },
      error: () => {
        this.loading = false;
        this.errorMsg = 'No se encontró el evento.';
      },
    });
  }

  irALogin(): void {
    this.router.navigate(['/login'], { queryParams: { returnUrl: this.router.url } });
  }

  private cargarPerfil(): void {
    const u = this.auth.getUsuario();
    if (!u) {
      return;
    }
    this.nombreCompleto = `${u.nombres} ${u.apellidos}`.trim();
    this.correoUsuario = u.correo;
    this.faltaTelefono = !u.telefono?.trim();
    if (u.telefono) {
      this.telefonoCtrl.setValue(u.telefono);
    }
  }

  guardarTelefono(): void {
    if (this.telefonoCtrl.invalid) {
      return;
    }
    this.guardandoTelefono = true;
    this.usuarioService
      .actualizarMiTelefono(this.telefonoCtrl.value!.trim())
      .pipe(
        tap(dto => {
          localStorage.setItem('usuario', JSON.stringify(dto));
          this.faltaTelefono = false;
        })
      )
      .subscribe({
        next: () => {
          this.guardandoTelefono = false;
        },
        error: err => {
          this.guardandoTelefono = false;
          this.errorMsg = err?.error?.mensaje || 'No se pudo guardar el teléfono.';
        },
      });
  }

  private refrescarCupo(id: number): void {
    this.eventoService.verificarCupo(id).subscribe({
      next: c => {
        this.hayCupo = c.hayCupoDisponible;
      },
      error: () => {
        this.hayCupo = false;
      },
    });
  }

  enviar(): void {
    this.errorMsg = null;
    this.exitoMsg = null;
    this.tokenQrRegistro = null;
    const u = this.auth.getUsuario();
    if (!u?.idUsuario || !this.evento) {
      this.irALogin();
      return;
    }
    if (this.faltaTelefono) {
      this.errorMsg = 'Registre su teléfono antes de continuar.';
      return;
    }
    const lista = this.campos ?? [];
    if (lista.length > 0 && this.dynFields && !this.dynFields.isValid()) {
      return;
    }
    const respuestas = this.dynFields?.buildPayload() ?? [];
    this.submitting = true;
    this.inscripcionService
      .crear({
        idUsuario: u.idUsuario,
        idEvento: this.evento.idEvento,
        respuestasCampos: respuestas,
      })
      .subscribe({
        next: res => {
          this.submitting = false;
          this.tokenQrRegistro = res.tokenQr ?? null;
          this.exitoMsg = this.tokenQrRegistro
            ? 'Inscripción confirmada. Guarde su QR de registro (pase de entrada).'
            : 'Inscripción registrada correctamente.';
        },
        error: err => {
          this.submitting = false;
          this.errorMsg = err?.error?.mensaje || 'No se pudo completar la inscripción.';
        },
      });
  }
}
