import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { forkJoin } from 'rxjs';
import { startWith } from 'rxjs';
import { Router } from '@angular/router';
import { AuthService } from '../../../Services/auth.service';
import { EventoService, ListarEventosFiltros } from '../../../Services/evento.service';
import {
  CrearEventoDTO,
  EventoDTO,
  EventoFilaAdminDTO,
  EventoResumenTipoDTO,
  ModalidadEvento,
  TipoEventoEnum,
  CrearCampoFormularioDTO,
  CampoFormularioDTO,
  TipoDatoCampo,
  UsuarioStaffDTO,
  CategoriaExhibicionFeria,
  NivelAcademico,
} from '../../../Models/evento-dto';
import { StaffAutocompleteComponent } from '../../../Components/staff-autocomplete/staff-autocomplete.component';
import { StackTagsPickerComponent } from '../../../Components/stack-tags-picker/stack-tags-picker.component';
import { QrCodeDisplayComponent } from '../../../Components/qr-code-display/qr-code-display.component';
import {
  etiquetaCategoriaFeria,
  etiquetaModalidad,
  etiquetaNivelCurso,
  etiquetaTipoCampo,
  etiquetaTipoEvento,
} from '../../../constants/ui-labels';
import { etiquetaEstadoEvento, EstadoOperativoEvento, ETIQUETAS_ESTADO_EVENTO } from '../../../constants/estado-evento';
import { TARJETAS_TIPO_EVENTO, TarjetaTipoEventoConfig } from '../../../constants/evento-tipo-cards';
import { URL_MAX_LENGTH, urlFlexibleValidator } from '../../../validators/url.validators';
import { etiquetaPrecioFormulario } from '../../../utils/currency.util';
import { AdminSidebarComponent } from '../../../Components/admin-sidebar/admin-sidebar.component';

@Component({
  selector: 'app-eventos',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    StaffAutocompleteComponent,
    StackTagsPickerComponent,
    QrCodeDisplayComponent,
    AdminSidebarComponent,
  ],
  templateUrl: './eventos.html',
  styleUrl: './eventos.scss',
})
export class Eventos implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  /** Sincronizado con `tipoEvento` del modal crear/editar para que la plantilla muestre bloques distintos. */
  readonly tipoCrearSig = signal<TipoEventoEnum | null>(null);
  readonly tipoEditSig = signal<TipoEventoEnum | null>(null);

  /** Listado completo para la tabla maestra (vista admin). */
  todosLosEventos: EventoFilaAdminDTO[] = [];
  /** Filas visibles en la tabla (filtradas por tarjeta de tipo). */
  eventosMostrados: EventoFilaAdminDTO[] = [];
  resumenesTipos: EventoResumenTipoDTO[] = [];
  tipoSeleccionado: TipoEventoEnum | null = null;
  readonly tarjetasTipo = TARJETAS_TIPO_EVENTO;
  sidebarCollapsed = false;
  loading = false;
  errorMsg: string | null = null;

  readonly tipos: TipoEventoEnum[] = ['CURSO', 'HACKATHON', 'TALLER', 'FERIA'];
  readonly modalidades: ModalidadEvento[] = ['PRESENCIAL', 'VIRTUAL', 'HIBRIDO'];
  readonly tiposDato: TipoDatoCampo[] = ['TEXTO', 'NUMERO', 'SELECT', 'CHECKBOX', 'URL', 'IMAGEN'];

  camposCrear: CrearCampoFormularioDTO[] = [];
  camposEdit: CampoFormularioDTO[] = [];
  profesoresSeleccionados: UsuarioStaffDTO[] = [];
  profesorLiderSeleccionado: UsuarioStaffDTO[] = [];
  monitoresSeleccionados: UsuarioStaffDTO[] = [];
  profesoresSeleccionadosEdit: UsuarioStaffDTO[] = [];
  profesorLiderSeleccionadoEdit: UsuarioStaffDTO[] = [];
  monitoresSeleccionadosEdit: UsuarioStaffDTO[] = [];
  stackTagsCrear: string[] = [];
  stackTagsEdit: string[] = [];
  imagenArchivo: File | null = null;
  imagenModeCrear: 'archivo' | 'url' = 'archivo';
  imagenModeEdit: 'archivo' | 'url' = 'archivo';
  imagenUrlCrear = '';
  imagenUrlEdit = '';
  pensumArchivo: File | null = null;
  eventoRecienCreado: EventoDTO | null = null;
  showQrDifusionModal = false;
  readonly categoriasFeria: CategoriaExhibicionFeria[] = [
    'SOFTWARE',
    'HARDWARE',
    'INTELIGENCIA_ARTIFICIAL',
    'INNOVACION_SOCIAL',
  ];
  readonly nivelesCurso: NivelAcademico[] = ['BASICO', 'INTERMEDIO', 'AVANZADO'];

  readonly etiquetaTipo = etiquetaTipoEvento;
  readonly etiquetaEstadoOp = etiquetaEstadoEvento;
  readonly etiquetaMod = etiquetaModalidad;
  readonly etiquetaCatFeria = etiquetaCategoriaFeria;
  readonly etiquetaNivel = etiquetaNivelCurso;

  filterForm!: FormGroup;
  createForm!: FormGroup;
  editForm!: FormGroup;

  readonly estadosOperativos: EstadoOperativoEvento[] = [
    'PROXIMO',
    'EN_CURSO',
    'FINALIZADO_POR_TIEMPO',
    'EN_REVISION',
    'CERRADO_Y_CERTIFICADO',
    'EVENT_CANCELLED',
  ];
  readonly etiquetasEstadoOp = ETIQUETAS_ESTADO_EVENTO;

  showReassignModal = false;
  reassignTarget: EventoFilaAdminDTO | null = null;
  profesoresReassign: UsuarioStaffDTO[] = [];
  monitoresReassign: UsuarioStaffDTO[] = [];
  guardandoReassign = false;
  showCreateModal = false;
  showEditModal = false;
  editing: EventoDTO | null = null;
  formAlert: string | null = null;
  guardando = false;
  intentoEnvioForm = false;

  readonly etiquetaTipoCampo = etiquetaTipoCampo;
  readonly urlMaxLength = URL_MAX_LENGTH;
  campoOpcionDraftCrear: Record<number, string> = {};
  campoOpcionDraftEdit: Record<number, string> = {};

  constructor(
    public authService: AuthService,
    private eventoService: EventoService,
    private fb: FormBuilder,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.filterForm = this.fb.group({
      soloActivos: [true],
      modalidad: [null as ModalidadEvento | null],
      tipo: [null as TipoEventoEnum | null],
      estadoOperativo: [null as EstadoOperativoEvento | null],
      desde: [''],
      hasta: [''],
    });
    this.createForm = this.buildEventoForm(true);
    this.editForm = this.buildEventoForm(false);
    this.sincronizarSignalsTipoEvento();
    this.sincronizarValidadoresModalidad(this.createForm);
    this.sincronizarValidadoresModalidad(this.editForm);
    if (this.authService.isStaff()) {
      this.loadDashboardData();
    }
  }

  /** Por si `valueChanges` no dispara la vista en algún entorno, el `change` nativo fuerza el signal. */
  onTipoCrearSelectChange(): void {
    const tipo = this.createForm.get('tipoEvento')?.value ?? null;
    this.tipoCrearSig.set(tipo);
    this.aplicarValidadoresPorTipo(this.createForm, tipo);
  }

  onTipoEditSelectChange(): void {
    const tipo = this.editForm.get('tipoEvento')?.value ?? null;
    this.tipoEditSig.set(tipo);
    this.aplicarValidadoresPorTipo(this.editForm, tipo);
  }

  get etiquetaPrecioCreate(): string {
    return etiquetaPrecioFormulario(this.createForm.get('precio')?.value);
  }

  get etiquetaPrecioEdit(): string {
    return etiquetaPrecioFormulario(this.editForm.get('precio')?.value);
  }

  onStackCrearChange(tags: string[]): void {
    this.stackTagsCrear = tags;
    this.createForm.patchValue({ stackTecnologico: JSON.stringify(tags) }, { emitEvent: false });
    this.createForm.get('stackTecnologico')?.updateValueAndValidity({ emitEvent: false });
  }

  onStackEditChange(tags: string[]): void {
    this.stackTagsEdit = tags;
    this.editForm.patchValue({ stackTecnologico: JSON.stringify(tags) }, { emitEvent: false });
    this.editForm.get('stackTecnologico')?.updateValueAndValidity({ emitEvent: false });
  }

  private parseStackJson(json: string | null | undefined): string[] {
    if (!json?.trim()) {
      return [];
    }
    try {
      const arr = JSON.parse(json) as unknown;
      return Array.isArray(arr) ? arr.map(String) : [];
    } catch {
      return [];
    }
  }

  cerrarQrDifusionModal(): void {
    this.showQrDifusionModal = false;
    this.eventoRecienCreado = null;
    this.loadDashboardData();
  }

  private sincronizarSignalsTipoEvento(): void {
    const cTipo = this.createForm.get('tipoEvento');
    if (cTipo) {
      const syncCrear = (v: TipoEventoEnum | null) => {
        this.tipoCrearSig.set(v);
        this.aplicarValidadoresPorTipo(this.createForm, v);
      };
      syncCrear(cTipo.value ?? null);
      cTipo.valueChanges
        .pipe(startWith(cTipo.value), takeUntilDestroyed(this.destroyRef))
        .subscribe(v => syncCrear(v ?? null));
    }
    const eTipo = this.editForm.get('tipoEvento');
    if (eTipo) {
      const syncEdit = (v: TipoEventoEnum | null) => {
        this.tipoEditSig.set(v);
        this.aplicarValidadoresPorTipo(this.editForm, v);
      };
      syncEdit(eTipo.value ?? null);
      eTipo.valueChanges
        .pipe(startWith(eTipo.value), takeUntilDestroyed(this.destroyRef))
        .subscribe(v => syncEdit(v ?? null));
    }
  }

  get usuario() {
    return this.authService.getUsuario();
  }

  get puedeCrearEditar(): boolean {
    return this.authService.hasRole('ADMIN', 'PROFESOR');
  }

  get minFechaInicioCreate(): string {
    const d = new Date();
    d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
    return d.toISOString().slice(0, 16);
  }

  toggleSidebar(): void {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  aplicarFiltros(): void {
    this.tipoSeleccionado = null;
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.loading = true;
    this.errorMsg = null;
    const v = this.filterForm.value;
    const filtros: ListarEventosFiltros = {
      soloActivos: v.soloActivos,
      modalidad: v.modalidad || undefined,
      tipo: v.tipo || undefined,
      estadoOperativo: v.estadoOperativo || undefined,
      desde: v.desde ? `${v.desde}T00:00:00` : undefined,
      hasta: v.hasta ? `${v.hasta}T23:59:59` : undefined,
    };
    forkJoin({
      resumenes: this.eventoService.resumenTipos(filtros),
      filas: this.eventoService.vistaAdmin(filtros),
    }).subscribe({
      next: ({ resumenes, filas }) => {
        this.resumenesTipos = resumenes;
        this.todosLosEventos = filas;
        this.aplicarFiltroTipoEnTabla();
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.errorMsg = this.mensajeErrorHttp(err, 'No se pudo cargar el dashboard de eventos.');
        console.error('[eventos] error cargando dashboard', err);
      },
    });
  }

  filtrarPorTipo(tipo: TipoEventoEnum): void {
    if (this.tipoSeleccionado === tipo) {
      this.tipoSeleccionado = null;
    } else {
      this.tipoSeleccionado = tipo;
    }
    this.aplicarFiltroTipoEnTabla();
  }

  private aplicarFiltroTipoEnTabla(): void {
    if (!this.tipoSeleccionado) {
      this.eventosMostrados = [...this.todosLosEventos];
      return;
    }
    this.eventosMostrados = this.todosLosEventos.filter(e => e.tipoEvento === this.tipoSeleccionado);
  }

  resumenPorTipo(tipo: TipoEventoEnum): EventoResumenTipoDTO | undefined {
    return this.resumenesTipos.find(r => r.tipo === tipo);
  }

  etiquetaContadorTarjeta(cfg: TarjetaTipoEventoConfig): string {
    const total = this.resumenPorTipo(cfg.tipo)?.totalEventos ?? 0;
    const etiqueta = total === 1 ? cfg.counterSingular : cfg.counterPlural;
    return `${total} ${etiqueta}`;
  }

  porcentajeOcupacionTarjeta(tipo: TipoEventoEnum): number {
    return this.resumenPorTipo(tipo)?.porcentajeOcupacion ?? 0;
  }

  badgeTipoClass(tipo: TipoEventoEnum | string): string {
    switch (tipo) {
      case 'CURSO':
        return 'badge-tipo-curso';
      case 'HACKATHON':
        return 'badge-tipo-hackathon';
      case 'FERIA':
        return 'badge-tipo-feria';
      case 'TALLER':
        return 'badge-tipo-taller';
      default:
        return 'badge-tipo-default';
    }
  }

  openCreateModal(): void {
    this.camposCrear = [];
    this.profesoresSeleccionados = [];
    this.profesorLiderSeleccionado = [];
    this.monitoresSeleccionados = [];
    this.campoOpcionDraftCrear = {};
    this.stackTagsCrear = [];
    this.imagenArchivo = null;
    this.imagenModeCrear = 'archivo';
    this.imagenUrlCrear = '';
    this.pensumArchivo = null;
    this.createForm.reset({
      nombreEvento: '',
      descripcion: '',
      tipoEvento: null,
      modalidad: null,
      fechaInicio: '',
      fechaFin: '',
      ubicacion: '',
      enlaceVirtual: '',
      aforoMaximo: null,
      intensidadHoraria: null,
      precio: 0,
      textoDiploma: '',
      firmaDigitalProfesor: '',
      imagenPromocionalUrl: '',
      nivelAcademico: null,
      notaMinimaAprobacion: null,
      porcentajeAsistenciaMinimo: 80,
      retoTecnicoCentral: '',
      minIntegrantes: 2,
      maxIntegrantes: 5,
      premiosIncentivos: '',
      categoriaExhibicion: null,
      stackTecnologico: '[]',
      criteriosEvaluacion: '',
      materialGuia: '',
    });
    this.tipoCrearSig.set(null);
    this.formAlert = null;
    this.intentoEnvioForm = false;
    this.aplicarValidadoresPorTipo(this.createForm, null);
    this.aplicarValidadoresModalidad(this.createForm, null);
    this.showCreateModal = true;
  }

  onImagenSelected(ev: Event): void {
    const input = ev.target as HTMLInputElement;
    this.imagenArchivo = input.files?.[0] ?? null;
  }

  onImagenDrop(ev: DragEvent): void {
    ev.preventDefault();
    const file = ev.dataTransfer?.files?.[0] ?? null;
    if (!file) {
      return;
    }
    this.imagenArchivo = file;
  }

  onImagenDragOver(ev: DragEvent): void {
    ev.preventDefault();
  }

  cambiarImagenModoCrear(modo: 'archivo' | 'url'): void {
    this.imagenModeCrear = modo;
    if (modo === 'url') {
      this.imagenArchivo = null;
    } else {
      this.imagenUrlCrear = '';
      this.createForm.patchValue({ imagenPromocionalUrl: '' }, { emitEvent: false });
    }
  }

  cambiarImagenModoEdit(modo: 'archivo' | 'url'): void {
    this.imagenModeEdit = modo;
    if (modo === 'archivo') {
      this.imagenUrlEdit = '';
      this.editForm.patchValue({ imagenPromocionalUrl: '' }, { emitEvent: false });
    }
  }

  onPensumSelected(ev: Event): void {
    const input = ev.target as HTMLInputElement;
    this.pensumArchivo = input.files?.[0] ?? null;
  }

  closeCreateModal(): void {
    this.showCreateModal = false;
    this.formAlert = null;
    this.guardando = false;
  }

  openEditModal(ev: Pick<EventoFilaAdminDTO, 'idEvento'>): void {
    this.eventoService.obtener(ev.idEvento).subscribe({
      next: full => {
        this.editing = full;
        this.camposEdit = (full.camposPersonalizados ?? []).map(c => ({ ...c }));
        this.campoOpcionDraftEdit = {};
        this.profesoresSeleccionadosEdit = [...(full.profesoresColaboradores ?? [])];
        const idLider = full.idProfesorLider ?? full.idUsuarioCreador ?? null;
        const liderObj = full.profesorLider
          ?? (full.profesoresColaboradores ?? []).find(p => p.idUsuario === idLider)
          ?? null;
        this.profesorLiderSeleccionadoEdit = liderObj ? [liderObj] : [];
        this.monitoresSeleccionadosEdit = [...(full.monitoresAsignados ?? [])];
        this.stackTagsEdit = this.parseStackJson(full.detalleFeria?.stackTecnologico);
        this.imagenUrlEdit = full.imagenPromocionalUrl ?? full.rutaImagenPromocional ?? '';
        this.imagenModeEdit = this.imagenUrlEdit ? 'url' : 'archivo';
        this.editForm.patchValue({
          nombreEvento: full.nombreEvento,
          descripcion: full.descripcion ?? '',
          tipoEvento: full.tipoEvento,
          modalidad: full.modalidad,
          fechaInicio: this.toDatetimeLocalValue(full.fechaInicio),
          fechaFin: this.toDatetimeLocalValue(full.fechaFin),
          ubicacion: full.ubicacion ?? '',
          enlaceVirtual: full.enlaceVirtual ?? '',
          aforoMaximo: full.aforoMaximo,
          intensidadHoraria: full.intensidadHoraria,
          precio: full.precio ?? 0,
          textoDiploma: full.textoDiploma ?? '',
          firmaDigitalProfesor: full.firmaDigitalProfesor ?? '',
          imagenPromocionalUrl: this.imagenUrlEdit,
          nivelAcademico: full.detalleCurso?.nivelAcademico ?? null,
          notaMinimaAprobacion: full.detalleCurso?.notaMinimaAprobacion ?? null,
          porcentajeAsistenciaMinimo:
            full.porcentajeAsistenciaMinimo ?? full.detalleCurso?.porcentajeAsistenciaMinimo ?? 80,
          retoTecnicoCentral: full.detalleHackathon?.retoTecnicoCentral ?? '',
          minIntegrantes: full.detalleHackathon?.minIntegrantes ?? 2,
          maxIntegrantes: full.detalleHackathon?.maxIntegrantes ?? 5,
          premiosIncentivos: full.detalleHackathon?.premiosIncentivos ?? '',
          categoriaExhibicion: full.detalleFeria?.categoriaExhibicion ?? null,
          stackTecnologico: full.detalleFeria?.stackTecnologico ?? '[]',
          criteriosEvaluacion: full.detalleFeria?.criteriosEvaluacion ?? '',
          materialGuia: full.detalleTaller?.materialGuia ?? '',
          estado: full.estado ?? true,
        });
        this.aplicarValidadoresPorTipo(this.editForm, full.tipoEvento);
        this.aplicarValidadoresModalidad(this.editForm, full.modalidad);
        this.tipoEditSig.set(full.tipoEvento);
        this.showEditModal = true;
      },
      error: () => (this.errorMsg = 'No se pudo cargar el detalle del evento.'),
    });
  }

  closeEditModal(): void {
    this.showEditModal = false;
    this.editing = null;
    this.camposEdit = [];
    this.profesorLiderSeleccionadoEdit = [];
    this.imagenUrlEdit = '';
    this.imagenModeEdit = 'archivo';
    this.formAlert = null;
    this.guardando = false;
  }

  submitCreate(): void {
    this.formAlert = null;
    this.intentoEnvioForm = true;
    const alertaStaff = this.validarReglasStaff(
      this.profesorLiderSeleccionado,
      this.profesoresSeleccionados,
      this.monitoresSeleccionados,
    );
    if (alertaStaff) {
      this.formAlert = alertaStaff;
      return;
    }
    this.sincronizarValidadoresAntesDeEnviar(this.createForm);
    this.logEstadoFormulario(this.createForm, 'crear-antes-validar');
    if (this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      this.formAlert = this.resumirErroresFormulario(this.createForm);
      return;
    }
    const dto = this.armarCrearDto();
    if (!dto) {
      return;
    }
    console.log('[crear evento] payload', dto);
    this.guardando = true;
    this.eventoService.crear(dto, this.imagenArchivo, this.pensumArchivo).subscribe({
      next: ev => {
        this.guardando = false;
        this.closeCreateModal();
        this.eventoRecienCreado = ev;
        this.showQrDifusionModal = !!ev.urlInscripcionPublica;
        this.loadDashboardData();
      },
      error: err => {
        this.guardando = false;
        this.formAlert = this.mensajeErrorHttp(err, 'Error al crear el evento.');
      },
    });
  }

  private armarCrearDto(): CrearEventoDTO | null {
    const raw = this.createForm.getRawValue();
    const tipo = this.normalizarTipoEvento(raw.tipoEvento);
    if (!tipo) {
      this.formAlert = 'Seleccione un tipo de evento válido (Curso, Hackathon, Taller o Feria).';
      return null;
    }
    const { ubicacion, enlaceVirtual } = this.normalizarUbicacionEnlace(
      raw.modalidad,
      raw.ubicacion,
      raw.enlaceVirtual,
    );
    const asistencia = Math.trunc(Number(raw.porcentajeAsistenciaMinimo));
    if (!Number.isFinite(asistencia) || asistencia < 1 || asistencia > 100) {
      this.formAlert = 'Indique la asistencia mínima (1–100 %) requerida para certificar.';
      return null;
    }
    const dto: CrearEventoDTO = {
      nombreEvento: raw.nombreEvento,
      descripcion: raw.descripcion || null,
      tipoEvento: tipo,
      modalidad: raw.modalidad,
      fechaInicio: this.normalizeLocalDateTime(raw.fechaInicio),
      fechaFin: this.normalizeLocalDateTime(raw.fechaFin),
      ubicacion,
      enlaceVirtual,
      aforoMaximo: Math.max(1, Math.trunc(Number(raw.aforoMaximo) || 0)),
      intensidadHoraria: Math.max(1, Math.trunc(Number(raw.intensidadHoraria) || 0)),
      porcentajeAsistenciaMinimo: asistencia,
      precio: Number(raw.precio) || 0,
      textoDiploma: raw.textoDiploma || null,
      firmaDigitalProfesor: raw.firmaDigitalProfesor || null,
      idProfesorLider: this.idProfesorLiderDesdeSeleccion(this.profesorLiderSeleccionado)!,
      imagenPromocionalUrl:
        this.imagenModeCrear === 'url'
          ? (raw.imagenPromocionalUrl?.trim() || null)
          : null,
      idsProfesoresColaboradores: this.idsStaffUnicos(this.profesoresSeleccionados),
      idsMonitoresAsignados: this.idsStaffUnicos(this.monitoresSeleccionados),
      camposPersonalizados: this.camposCrearValidos(),
    };
    switch (tipo) {
      case 'CURSO': {
        const nota = Number(raw.notaMinimaAprobacion);
        if (!raw.nivelAcademico || !Number.isFinite(nota)) {
          this.formAlert = 'Complete nivel académico y nota mínima del curso antes de guardar.';
          return null;
        }
        dto.detalleCurso = {
          nivelAcademico: raw.nivelAcademico,
          notaMinimaAprobacion: nota,
          porcentajeAsistenciaMinimo: asistencia,
        };
        break;
      }
      case 'HACKATHON':
        dto.detalleHackathon = {
          retoTecnicoCentral: raw.retoTecnicoCentral,
          minIntegrantes: Number(raw.minIntegrantes),
          maxIntegrantes: Number(raw.maxIntegrantes),
          premiosIncentivos: raw.premiosIncentivos || null,
        };
        break;
      case 'FERIA':
        dto.detalleFeria = {
          categoriaExhibicion: raw.categoriaExhibicion,
          stackTecnologico: JSON.stringify(this.stackTagsCrear),
          criteriosEvaluacion: raw.criteriosEvaluacion,
        };
        break;
      case 'TALLER':
        dto.detalleTaller = { materialGuia: raw.materialGuia || null };
        break;
    }
    return dto;
  }

  submitEdit(): void {
    this.formAlert = null;
    this.intentoEnvioForm = true;
    if (!this.editing) {
      return;
    }
    const alertaStaff = this.validarReglasStaff(
      this.profesorLiderSeleccionadoEdit,
      this.profesoresSeleccionadosEdit,
      this.monitoresSeleccionadosEdit,
    );
    if (alertaStaff) {
      this.formAlert = alertaStaff;
      return;
    }
    this.sincronizarValidadoresAntesDeEnviar(this.editForm);
    if (this.editForm.invalid) {
      this.editForm.markAllAsTouched();
      this.formAlert = this.resumirErroresFormulario(this.editForm);
      return;
    }
    const raw = this.editForm.getRawValue();
    const tipo = raw.tipoEvento as TipoEventoEnum;
    const { ubicacion, enlaceVirtual } = this.normalizarUbicacionEnlace(
      raw.modalidad,
      raw.ubicacion,
      raw.enlaceVirtual,
    );
    const dto: EventoDTO = {
      idEvento: this.editing.idEvento,
      nombreEvento: raw.nombreEvento,
      descripcion: raw.descripcion || null,
      tipoEvento: tipo,
      modalidad: raw.modalidad,
      fechaInicio: this.normalizeLocalDateTime(raw.fechaInicio),
      fechaFin: this.normalizeLocalDateTime(raw.fechaFin),
      ubicacion,
      enlaceVirtual,
      aforoMaximo: Number(raw.aforoMaximo),
      intensidadHoraria: Number(raw.intensidadHoraria),
      precio: Number(raw.precio) || 0,
      textoDiploma: raw.textoDiploma || null,
      firmaDigitalProfesor: raw.firmaDigitalProfesor || null,
      idProfesorLider: this.idProfesorLiderDesdeSeleccion(this.profesorLiderSeleccionadoEdit),
      imagenPromocionalUrl:
        this.imagenModeEdit === 'url'
          ? (raw.imagenPromocionalUrl?.trim() || null)
          : null,
      estado: raw.estado,
      idUsuarioCreador: this.editing.idUsuarioCreador,
      idsProfesoresColaboradores: this.idsStaffUnicos(this.profesoresSeleccionadosEdit),
      idsMonitoresAsignados: this.idsStaffUnicos(this.monitoresSeleccionadosEdit),
      camposPersonalizados: this.camposEditValidos(),
      porcentajeAsistenciaMinimo: Number(raw.porcentajeAsistenciaMinimo),
    };
    if (tipo === 'CURSO') {
      dto.detalleCurso = {
        nivelAcademico: raw.nivelAcademico,
        notaMinimaAprobacion: Number(raw.notaMinimaAprobacion),
        porcentajeAsistenciaMinimo: Number(raw.porcentajeAsistenciaMinimo),
      };
    } else if (tipo === 'HACKATHON') {
      dto.detalleHackathon = {
        retoTecnicoCentral: raw.retoTecnicoCentral,
        minIntegrantes: Number(raw.minIntegrantes),
        maxIntegrantes: Number(raw.maxIntegrantes),
        premiosIncentivos: raw.premiosIncentivos || null,
      };
    } else if (tipo === 'FERIA') {
      dto.detalleFeria = {
        categoriaExhibicion: raw.categoriaExhibicion,
        stackTecnologico: JSON.stringify(this.stackTagsEdit),
        criteriosEvaluacion: raw.criteriosEvaluacion,
      };
    } else if (tipo === 'TALLER') {
      dto.detalleTaller = { materialGuia: raw.materialGuia || null };
    }
    this.guardando = true;
    this.eventoService.actualizar(this.editing.idEvento, dto).subscribe({
      next: () => {
        this.guardando = false;
        this.closeEditModal();
        this.loadDashboardData();
      },
      error: err => {
        this.guardando = false;
        this.formAlert = this.mensajeErrorHttp(err, 'Error al actualizar el evento.');
      },
    });
  }

  desactivar(ev: EventoFilaAdminDTO): void {
    if (!confirm(`¿Cancelar el evento "${ev.nombreEvento}"? (soft delete)`)) {
      return;
    }
    this.eventoService.cancelarEvento(ev.idEvento).subscribe({
      next: () => this.loadDashboardData(),
      error: err => (this.errorMsg = err?.error?.mensaje || 'No se pudo cancelar el evento.'),
    });
  }

  forzarCierre(ev: EventoFilaAdminDTO): void {
    if (!this.puedeForzarCierre(ev)) {
      return;
    }
    if (!confirm(`¿Forzar cierre y certificación de emergencia para "${ev.nombreEvento}"?`)) {
      return;
    }
    this.eventoService.forzarCierre(ev.idEvento).subscribe({
      next: res => {
        this.formAlert = res.mensaje;
        this.loadDashboardData();
      },
      error: err => (this.errorMsg = err?.error?.mensaje || 'No se pudo forzar el cierre.'),
    });
  }

  puedeForzarCierre(ev: EventoFilaAdminDTO): boolean {
    return (
      this.authService.isAdmin() &&
      (ev.estadoOperativo === 'EN_REVISION' || ev.estadoOperativo === 'FINALIZADO_POR_TIEMPO')
    );
  }

  openReassignStaff(ev: EventoFilaAdminDTO): void {
    this.reassignTarget = ev;
    this.profesoresReassign = [];
    this.monitoresReassign = [];
    this.showReassignModal = true;
    this.eventoService.obtener(ev.idEvento).subscribe({
      next: (dto) => {
        this.profesoresReassign = [...(dto.profesoresColaboradores ?? [])];
        this.monitoresReassign = [...(dto.monitoresAsignados ?? [])];
      },
      error: () => {
        this.errorMsg = 'No se pudo cargar el personal del evento.';
      },
    });
  }

  closeReassignModal(): void {
    this.showReassignModal = false;
    this.reassignTarget = null;
  }

  saveReassignStaff(): void {
    if (!this.reassignTarget) return;
    this.guardandoReassign = true;
    this.eventoService
      .reasignarStaff(this.reassignTarget.idEvento, {
        idsProfesoresColaboradores: this.profesoresReassign.map(p => p.idUsuario),
        idsMonitoresAsignados: this.monitoresReassign.map(m => m.idUsuario),
      })
      .subscribe({
        next: () => {
          this.guardandoReassign = false;
          this.closeReassignModal();
          this.loadDashboardData();
        },
        error: err => {
          this.guardandoReassign = false;
          this.errorMsg = err?.error?.mensaje || 'No se pudo reasignar el personal.';
        },
      });
  }

  esModalidadVirtual(ctrl: AbstractControl | null): boolean {
    return ctrl?.value === 'VIRTUAL';
  }

  esModalidadHibrida(ctrl: AbstractControl | null): boolean {
    return ctrl?.value === 'HIBRIDO';
  }

  requiereUbicacion(ctrl: AbstractControl | null): boolean {
    const m = ctrl?.value;
    return m === 'PRESENCIAL' || m === 'HIBRIDO';
  }

  requiereEnlace(ctrl: AbstractControl | null): boolean {
    const m = ctrl?.value;
    return m === 'VIRTUAL' || m === 'HIBRIDO';
  }

  onModalidadCrearChange(): void {
    this.aplicarValidadoresModalidad(this.createForm, this.createForm.get('modalidad')?.value);
  }

  onModalidadEditChange(): void {
    this.aplicarValidadoresModalidad(this.editForm, this.editForm.get('modalidad')?.value);
  }

  private sincronizarValidadoresModalidad(form: FormGroup): void {
    form.get('modalidad')?.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(m => {
      this.aplicarValidadoresModalidad(form, m);
    });
  }

  private aplicarValidadoresModalidad(form: FormGroup, modalidad: ModalidadEvento | null): void {
    const ubicacion = form.get('ubicacion');
    const enlace = form.get('enlaceVirtual');
    ubicacion?.clearValidators();
    enlace?.clearValidators();

    if (modalidad === 'PRESENCIAL' || modalidad === 'HIBRIDO') {
      ubicacion?.setValidators([Validators.required, Validators.maxLength(URL_MAX_LENGTH)]);
    } else {
      ubicacion?.setValidators([Validators.maxLength(URL_MAX_LENGTH)]);
    }

    if (modalidad === 'VIRTUAL' || modalidad === 'HIBRIDO') {
      enlace?.setValidators([
        Validators.required,
        Validators.maxLength(URL_MAX_LENGTH),
        urlFlexibleValidator(),
      ]);
    } else {
      enlace?.setValue('', { emitEvent: false });
    }

    ubicacion?.updateValueAndValidity({ emitEvent: false });
    enlace?.updateValueAndValidity({ emitEvent: false });
  }

  private sincronizarValidadoresAntesDeEnviar(form: FormGroup): void {
    const tipo = form.get('tipoEvento')?.value as TipoEventoEnum | null;
    const modalidad = form.get('modalidad')?.value as ModalidadEvento | null;
    this.aplicarValidadoresPorTipo(form, tipo);
    this.aplicarValidadoresModalidad(form, modalidad);
    form.updateValueAndValidity({ emitEvent: false });
  }

  campoMuestraError(form: FormGroup, nombre: string): boolean {
    const c = form.get(nombre);
    return !!(c && c.invalid && (c.touched || this.intentoEnvioForm));
  }

  private buildEventoForm(esCreacion: boolean): FormGroup {
    return this.fb.group(
      {
        nombreEvento: ['', [Validators.required, Validators.maxLength(150)]],
        descripcion: ['', Validators.maxLength(8000)],
        tipoEvento: [null as TipoEventoEnum | null, Validators.required],
        modalidad: [null as ModalidadEvento | null, Validators.required],
        fechaInicio: ['', [Validators.required, ...(esCreacion ? [this.fechaInicioNoPasada()] : [])]],
        fechaFin: ['', Validators.required],
        ubicacion: ['', Validators.maxLength(URL_MAX_LENGTH)],
        enlaceVirtual: ['', [Validators.maxLength(URL_MAX_LENGTH), urlFlexibleValidator()]],
        aforoMaximo: [null as number | null, [this.numeroRequeridoMin(1)]],
        intensidadHoraria: [null as number | null, [this.numeroRequeridoMin(1)]],
        precio: [0, [Validators.required, Validators.min(0)]],
        textoDiploma: [''],
        firmaDigitalProfesor: [''],
        imagenPromocionalUrl: ['', [Validators.maxLength(10000), urlFlexibleValidator()]],
        nivelAcademico: [null as NivelAcademico | null],
        notaMinimaAprobacion: [null as number | null],
        porcentajeAsistenciaMinimo: [80, [Validators.required, Validators.min(1), Validators.max(100)]],
        retoTecnicoCentral: [''],
        minIntegrantes: [2],
        maxIntegrantes: [5],
        premiosIncentivos: [''],
        categoriaExhibicion: [null as CategoriaExhibicionFeria | null],
        stackTecnologico: ['[]'],
        criteriosEvaluacion: [''],
        materialGuia: [''],
        ...(esCreacion ? {} : { estado: [true] }),
      },
      { validators: [this.rangoFechasValido()] }
    );
  }

  private readonly camposPorTipo: Record<TipoEventoEnum, string[]> = {
    CURSO: ['nivelAcademico', 'notaMinimaAprobacion'],
    HACKATHON: ['retoTecnicoCentral', 'minIntegrantes', 'maxIntegrantes'],
    FERIA: ['categoriaExhibicion', 'stackTecnologico', 'criteriosEvaluacion'],
    TALLER: ['materialGuia'],
  };

  private readonly valoresPorDefectoTipoOculto: Record<string, unknown> = {
    nivelAcademico: null,
    notaMinimaAprobacion: null,
    porcentajeAsistenciaMinimo: 80,
    retoTecnicoCentral: '',
    minIntegrantes: 2,
    maxIntegrantes: 5,
    categoriaExhibicion: null,
    stackTecnologico: '[]',
    criteriosEvaluacion: '',
    materialGuia: '',
  };

  private aplicarValidadoresPorTipo(form: FormGroup, tipo: TipoEventoEnum | null): void {
    const activos = tipo ? (this.camposPorTipo[tipo] ?? []) : [];
    const todos = Object.values(this.camposPorTipo).flat();

    for (const nombre of todos) {
      const ctrl = form.get(nombre);
      if (!ctrl) {
        continue;
      }
      ctrl.clearValidators();
      if (!activos.includes(nombre)) {
        const defecto = this.valoresPorDefectoTipoOculto[nombre];
        ctrl.setValue(defecto ?? null, { emitEvent: false });
        ctrl.setErrors(null);
        ctrl.markAsPristine();
        ctrl.markAsUntouched();
      }
      ctrl.updateValueAndValidity({ emitEvent: false });
    }

    form
      .get('porcentajeAsistenciaMinimo')
      ?.setValidators([Validators.required, Validators.min(1), Validators.max(100)]);

    if (tipo === 'CURSO') {
      form.get('nivelAcademico')?.setValidators(Validators.required);
      form
        .get('notaMinimaAprobacion')
        ?.setValidators([Validators.required, Validators.min(0), Validators.max(5)]);
    } else if (tipo === 'HACKATHON') {
      form.get('retoTecnicoCentral')?.setValidators([Validators.required, Validators.maxLength(2000)]);
      form.get('minIntegrantes')?.setValidators([Validators.required, this.numeroRequeridoMin(1)]);
      form.get('maxIntegrantes')?.setValidators([Validators.required, this.numeroRequeridoMin(1)]);
    } else if (tipo === 'FERIA') {
      form.get('categoriaExhibicion')?.setValidators(Validators.required);
      form.get('criteriosEvaluacion')?.setValidators([Validators.required, Validators.maxLength(4000)]);
      form.get('stackTecnologico')?.setValidators(this.stackFeriaRequerido());
    }

    form.setValidators(
      tipo === 'HACKATHON'
        ? [this.rangoFechasValido(), this.hackathonIntegrantesValido()]
        : [this.rangoFechasValido()],
    );
    form.updateValueAndValidity({ emitEvent: false });
    todos.forEach(c => form.get(c)?.updateValueAndValidity({ emitEvent: false }));
  }

  private fechaInicioNoPasada(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const v = control.value as string;
      if (!v) {
        return null;
      }
      const inicio = this.parseDatetimeLocal(v);
      if (!inicio) {
        return { fechaInvalida: true };
      }
      const ahora = new Date();
      if (inicio.getTime() < ahora.getTime() - 60_000) {
        return { pasado: true };
      }
      return null;
    };
  }

  private numeroRequeridoMin(min: number): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const raw = control.value;
      if (raw === null || raw === undefined || raw === '') {
        return { required: true };
      }
      const n = Number(raw);
      if (!Number.isFinite(n) || n < min) {
        return { min: { min, actual: n } };
      }
      return null;
    };
  }

  private parseDatetimeLocal(v: string): Date | null {
    if (!v?.trim()) {
      return null;
    }
    const normalized = this.normalizeLocalDateTime(v.trim());
    const [datePart, timePart = '00:00:00'] = normalized.split('T');
    const [y, m, d] = datePart.split('-').map(Number);
    const [hh, mm, ss = 0] = timePart.split(':').map(Number);
    if (![y, m, d, hh, mm].every(Number.isFinite)) {
      return null;
    }
    return new Date(y, m - 1, d, hh, mm, Number.isFinite(ss) ? ss : 0);
  }

  private logEstadoFormulario(form: FormGroup, etiqueta: string): void {
    console.log(`[${etiqueta}] form.value`, form.getRawValue());
    console.log(`[${etiqueta}] form.errors`, form.errors);
    Object.keys(form.controls).forEach(key => {
      const c = form.get(key);
      if (c?.invalid) {
        console.warn(`[${etiqueta}] inválido: ${key}`, c.errors, 'valor:', c.value);
      }
    });
  }

  private stackFeriaRequerido(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      try {
        const parsed = JSON.parse(String(control.value ?? '[]')) as unknown;
        if (Array.isArray(parsed) && parsed.length > 0) {
          return null;
        }
      } catch {
        /* JSON inválido */
      }
      return { stackVacio: true };
    };
  }

  private hackathonIntegrantesValido(): ValidatorFn {
    return (group: AbstractControl): ValidationErrors | null => {
      if (group.get('tipoEvento')?.value !== 'HACKATHON') {
        return null;
      }
      const min = Number(group.get('minIntegrantes')?.value);
      const max = Number(group.get('maxIntegrantes')?.value);
      if (Number.isFinite(min) && Number.isFinite(max) && max < min) {
        return { integrantesInvalidos: true };
      }
      return null;
    };
  }

  private rangoFechasValido(): ValidatorFn {
    return (group: AbstractControl): ValidationErrors | null => {
      const ini = group.get('fechaInicio')?.value as string;
      const fin = group.get('fechaFin')?.value as string;
      if (!ini || !fin) {
        return null;
      }
      const dIni = this.parseDatetimeLocal(ini);
      const dFin = this.parseDatetimeLocal(fin);
      if (!dIni || !dFin) {
        return { fechaInvalida: true };
      }
      if (dFin.getTime() < dIni.getTime()) {
        return { finAntesInicio: true };
      }
      return null;
    };
  }

  private toDatetimeLocalValue(iso: string): string {
    if (!iso) {
      return '';
    }
    if (iso.length >= 16) {
      return iso.slice(0, 16);
    }
    return iso;
  }

  private normalizeLocalDateTime(v: string): string {
    if (!v) {
      return v;
    }
    if (v.length === 16) {
      return `${v}:00`;
    }
    return v;
  }

  addCampoCrear(): void {
    this.camposCrear.push({
      etiqueta: '',
      tipoDato: 'TEXTO',
      esObligatorio: false,
      opciones: null,
    });
  }

  removeCampoCrear(i: number): void {
    this.camposCrear.splice(i, 1);
    delete this.campoOpcionDraftCrear[i];
  }

  addCampoEdit(): void {
    if (!this.editing) {
      return;
    }
    this.camposEdit.push({
      etiqueta: '',
      tipoDato: 'TEXTO',
      esObligatorio: false,
      opciones: null,
      idEvento: this.editing.idEvento,
    });
  }

  removeCampoEdit(i: number): void {
    this.camposEdit.splice(i, 1);
    delete this.campoOpcionDraftEdit[i];
  }

  onTipoDatoCampoCrearChange(i: number): void {
    const campo = this.camposCrear[i];
    if (!campo || campo.tipoDato === 'SELECT') return;
    campo.opciones = null;
    this.campoOpcionDraftCrear[i] = '';
  }

  onTipoDatoCampoEditChange(i: number): void {
    const campo = this.camposEdit[i];
    if (!campo || campo.tipoDato === 'SELECT') return;
    campo.opciones = null;
    this.campoOpcionDraftEdit[i] = '';
  }

  agregarOpcionCampoCrear(i: number): void {
    const draft = (this.campoOpcionDraftCrear[i] ?? '').trim();
    if (!draft) return;
    const campo = this.camposCrear[i];
    if (!campo || campo.tipoDato !== 'SELECT') return;
    const opciones = this.obtenerOpcionesCampo(campo);
    if (!opciones.includes(draft)) {
      opciones.push(draft);
      campo.opciones = JSON.stringify(opciones);
    }
    this.campoOpcionDraftCrear[i] = '';
  }

  agregarOpcionCampoEdit(i: number): void {
    const draft = (this.campoOpcionDraftEdit[i] ?? '').trim();
    if (!draft) return;
    const campo = this.camposEdit[i];
    if (!campo || campo.tipoDato !== 'SELECT') return;
    const opciones = this.obtenerOpcionesCampo(campo);
    if (!opciones.includes(draft)) {
      opciones.push(draft);
      campo.opciones = JSON.stringify(opciones);
    }
    this.campoOpcionDraftEdit[i] = '';
  }

  eliminarOpcionCampoCrear(i: number, opcion: string): void {
    const campo = this.camposCrear[i];
    if (!campo) return;
    const opciones = this.obtenerOpcionesCampo(campo).filter(o => o !== opcion);
    campo.opciones = opciones.length ? JSON.stringify(opciones) : null;
  }

  eliminarOpcionCampoEdit(i: number, opcion: string): void {
    const campo = this.camposEdit[i];
    if (!campo) return;
    const opciones = this.obtenerOpcionesCampo(campo).filter(o => o !== opcion);
    campo.opciones = opciones.length ? JSON.stringify(opciones) : null;
  }

  obtenerOpcionesCampo(campo: { opciones?: string | null }): string[] {
    const raw = campo.opciones?.trim();
    if (!raw) return [];
    try {
      const arr = JSON.parse(raw) as unknown;
      if (Array.isArray(arr)) {
        return arr.map(v => String(v).trim()).filter(Boolean);
      }
    } catch {
      return raw.split(',').map(v => v.trim()).filter(Boolean);
    }
    return [];
  }

  private camposCrearValidos(): CrearCampoFormularioDTO[] {
    return this.camposCrear
      .filter(c => c.etiqueta?.trim())
      .map(c => ({
        etiqueta: c.etiqueta.trim(),
        tipoDato: c.tipoDato,
        esObligatorio: !!c.esObligatorio,
        opciones: c.tipoDato === 'SELECT' ? this.serializarOpcionesCampo(c) : null,
      }));
  }

  /**
   * El API espera el enum como string ("CURSO"), no un objeto con id.
   */
  private normalizarTipoEvento(valor: unknown): TipoEventoEnum | null {
    if (valor == null || valor === '') {
      return null;
    }
    if (typeof valor === 'string') {
      const t = valor.toUpperCase();
      if (this.tipos.includes(t as TipoEventoEnum)) {
        return t as TipoEventoEnum;
      }
      return null;
    }
    return null;
  }

  private normalizarUbicacionEnlace(
    modalidad: ModalidadEvento | null,
    ubicacionRaw: string | null | undefined,
    enlaceRaw: string | null | undefined,
  ): { ubicacion: string | null; enlaceVirtual: string | null } {
    let ubicacion = ubicacionRaw?.trim() || null;
    let enlaceVirtual = enlaceRaw?.trim() || null;
    if (!enlaceVirtual && ubicacion && (modalidad === 'VIRTUAL' || modalidad === 'HIBRIDO')) {
      enlaceVirtual = ubicacion;
    }
    if (modalidad === 'VIRTUAL') {
      ubicacion = null;
    }
    if (modalidad === 'PRESENCIAL') {
      enlaceVirtual = null;
    }
    return { ubicacion, enlaceVirtual };
  }

  private idsStaffUnicos(lista: UsuarioStaffDTO[]): number[] {
    const ids = lista.map(p => p.idUsuario).filter((id): id is number => id != null && id > 0);
    return [...new Set(ids)];
  }

  private validarReglasStaff(
    liderSel: UsuarioStaffDTO[],
    profesoresSel: UsuarioStaffDTO[],
    monitoresSel: UsuarioStaffDTO[],
  ): string | null {
    const lider = this.idProfesorLiderDesdeSeleccion(liderSel);
    if (!lider) {
      return 'Debe seleccionar un profesor líder responsable del evento.';
    }
    const prof = new Set(this.idsStaffUnicos(profesoresSel));
    const mon = this.idsStaffUnicos(monitoresSel);
    if (mon.some(id => prof.has(id))) {
      return 'Un mismo usuario no puede ser profesor colaborador y monitor a la vez.';
    }
    if (mon.includes(lider)) {
      return 'El profesor líder no puede estar asignado también como monitor.';
    }
    return null;
  }

  private idProfesorLiderDesdeSeleccion(seleccion: UsuarioStaffDTO[]): number | null {
    const id = seleccion?.[0]?.idUsuario;
    return id != null && id > 0 ? id : null;
  }

  private mensajeErrorHttp(err: { error?: { mensaje?: string; detalles?: string[] } }, fallback: string): string {
    const body = err?.error;
    const base = body?.detalles?.length
      ? `${body.mensaje ?? fallback}: ${body.detalles.join('; ')}`
      : body?.mensaje || fallback;
    return `Error del servidor: ${base}`;
  }

  resumirErroresFormulario(form: FormGroup): string {
    const labels: Record<string, string> = {
      nombreEvento: 'Nombre del evento',
      tipoEvento: 'Tipo de evento',
      modalidad: 'Modalidad',
      fechaInicio: 'Fecha de inicio',
      fechaFin: 'Fecha de fin',
      ubicacion: 'Ubicación',
      enlaceVirtual: 'Enlace virtual',
      aforoMaximo: 'Aforo máximo',
      intensidadHoraria: 'Intensidad horaria',
      precio: 'Precio',
      nivelAcademico: 'Nivel académico',
      notaMinimaAprobacion: 'Nota mínima (0 a 5)',
      porcentajeAsistenciaMinimo: 'Asistencia mínima (%)',
      retoTecnicoCentral: 'Reto técnico',
      minIntegrantes: 'Mín. integrantes',
      maxIntegrantes: 'Máx. integrantes',
      categoriaExhibicion: 'Categoría de feria',
      criteriosEvaluacion: 'Criterios de evaluación',
      stackTecnologico: 'Stack tecnológico (feria)',
    };
    const faltantes: string[] = [];
    Object.keys(form.controls).forEach(key => {
      const c = form.get(key);
      if (!c?.invalid) {
        return;
      }
      const nombre = labels[key] ?? key;
      const errs = c.errors ?? {};
      if (errs['required']) {
        faltantes.push(nombre);
      } else if (errs['urlInvalida'] || errs['urlLarga']) {
        faltantes.push(`${nombre} (URL inválida o demasiado larga)`);
      } else if (errs['pasado']) {
        faltantes.push(`${nombre} (no puede ser en el pasado)`);
      } else if (errs['fechaInvalida']) {
        faltantes.push(`${nombre} (formato de fecha inválido)`);
      } else if (errs['min']) {
        faltantes.push(`${nombre} (valor demasiado bajo)`);
      } else if (errs['max']) {
        faltantes.push(`${nombre} (valor demasiado alto)`);
      } else if (errs['stackVacio']) {
        faltantes.push(`${nombre} (agregue al menos una tecnología)`);
      } else {
        faltantes.push(nombre);
      }
    });
    if (form.errors?.['finAntesInicio']) {
      faltantes.push('La fecha de fin debe ser posterior a la de inicio');
    }
    if (form.errors?.['fechaInvalida']) {
      faltantes.push('Revise el formato de las fechas de inicio y fin');
    }
    if (form.errors?.['integrantesInvalidos']) {
      faltantes.push('Integrantes máximos menores que el mínimo');
    }
    if (!faltantes.length) {
      return 'Revise los campos marcados antes de guardar.';
    }
    return `Complete o corrija: ${[...new Set(faltantes)].join(', ')}.`;
  }

  private camposEditValidos(): CampoFormularioDTO[] {
    return this.camposEdit
      .filter(c => c.etiqueta?.trim())
      .map(c => ({
        ...c,
        etiqueta: c.etiqueta.trim(),
        opciones: c.tipoDato === 'SELECT' ? this.serializarOpcionesCampo(c) : null,
      }));
  }

  private serializarOpcionesCampo(campo: { opciones?: string | null }): string | null {
    const opciones = this.obtenerOpcionesCampo(campo);
    return opciones.length ? JSON.stringify(opciones) : null;
  }
}
