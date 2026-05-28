import { Component, OnInit, ViewChild, inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { EventoService } from '../../../Services/evento.service';
import { InscripcionService } from '../../../Services/inscripcion.service';
import { AuthService } from '../../../Services/auth.service';
import { UsuarioService } from '../../../Services/usuario.service';
import { EventoDTO, CampoFormularioDTO, ModalidadEvento } from '../../../Models/evento-dto';
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
  templateUrl: './inscribir-evento.html',
  styleUrl: './inscribir-evento.scss',
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
  porcentajeAsistenciaMinimo: number | null = null;

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
            porcentajeAsistenciaMinimo: pub.porcentajeAsistenciaMinimo ?? 80,
            precio: pub.precio,
            gratuito: pub.gratuito,
            camposPersonalizados: pub.camposPersonalizados,
          };
          this.campos = pub.camposPersonalizados ?? [];
          this.porcentajeAsistenciaMinimo = pub.porcentajeAsistenciaMinimo ?? 80;
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
        this.porcentajeAsistenciaMinimo =
          ev.porcentajeAsistenciaMinimo ?? ev.detalleCurso?.porcentajeAsistenciaMinimo ?? 80;
        this.loading = false;
        this.refrescarCupo(id);
      },
      error: () => {
        this.loading = false;
        this.errorMsg = 'No se encontró el evento.';
      },
    });
  }

  etiquetaModalidad(modalidad: ModalidadEvento): string {
    const map: Record<ModalidadEvento, string> = {
      PRESENCIAL: 'Presencial',
      VIRTUAL: 'Virtual',
      HIBRIDO: 'Híbrido',
    };
    return map[modalidad] ?? modalidad;
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
