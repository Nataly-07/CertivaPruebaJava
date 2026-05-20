import { Component, Input, OnChanges, SimpleChanges, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  AbstractControl,
  FormBuilder,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { CampoFormularioDTO, RespuestaCampoDTO } from '../../Models/evento-dto';
import { URL_MAX_LENGTH, urlFlexibleValidator } from '../../validators/url.validators';

const IMAGEN_MAX_BYTES = 2 * 1024 * 1024;

@Component({
  selector: 'app-dynamic-event-fields',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './dynamic-event-fields.component.html',
  styles: [
    `
      .dynamic-img-preview {
        max-width: 100%;
        max-height: 160px;
        border-radius: 8px;
        object-fit: contain;
        border: 1px solid rgba(148, 163, 184, 0.25);
      }
    `,
  ],
})
export class DynamicEventFieldsComponent implements OnChanges {
  private fb = inject(FormBuilder);

  @Input() campos: CampoFormularioDTO[] = [];

  form: FormGroup = this.fb.group({});
  readonly imagenPreview = signal<Record<string, string>>({});
  readonly imagenError = signal<string | null>(null);
  readonly urlMaxLength = URL_MAX_LENGTH;

  ngOnChanges(changes: SimpleChanges): void {
    if (!changes['campos']) {
      return;
    }
    this.rebuildForm();
  }

  trackByCampo(index: number, c: CampoFormularioDTO): number | string {
    return c.idCampo ?? index;
  }

  key(c: CampoFormularioDTO, idx: number): string {
    const id = c.idCampo ?? idx;
    return 'c_' + id;
  }

  private rebuildForm(): void {
    if (!this.campos?.length) {
      this.form = this.fb.group({});
      this.imagenPreview.set({});
      return;
    }
    const cfg: Record<string, FormControl<unknown>> = {};
    for (let i = 0; i < this.campos.length; i++) {
      const c = this.campos[i];
      const validators: ValidatorFn[] = [];
      if (c.esObligatorio) {
        validators.push(Validators.required);
      }
      if (c.tipoDato === 'NUMERO') {
        validators.push(this.numeroValido);
      }
      if (c.tipoDato === 'URL') {
        validators.push(urlFlexibleValidator(), Validators.maxLength(URL_MAX_LENGTH));
      }
      let initial: string | boolean = '';
      if (c.tipoDato === 'CHECKBOX') {
        initial = '';
      }
      cfg[this.key(c, i)] = new FormControl(initial, { validators });
    }
    this.form = this.fb.group(cfg);
    this.imagenPreview.set({});
  }

  onImagenSelected(ev: Event, c: CampoFormularioDTO, idx: number): void {
    this.imagenError.set(null);
    const input = ev.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }
    if (!file.type.startsWith('image/')) {
      this.imagenError.set('Seleccione un archivo de imagen (JPEG, PNG, WebP o GIF).');
      input.value = '';
      return;
    }
    if (file.size > IMAGEN_MAX_BYTES) {
      this.imagenError.set('La imagen no debe superar 2 MB.');
      input.value = '';
      return;
    }
    const reader = new FileReader();
    reader.onload = () => {
      const dataUrl = String(reader.result ?? '');
      const k = this.key(c, idx);
      this.form.get(k)?.setValue(dataUrl);
      this.imagenPreview.update(prev => ({ ...prev, [k]: dataUrl }));
    };
    reader.readAsDataURL(file);
  }

  previewUrl(c: CampoFormularioDTO, idx: number): string | null {
    const k = this.key(c, idx);
    return this.imagenPreview()[k] ?? null;
  }

  private numeroValido: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
    const v = control.value;
    if (v === null || v === undefined || String(v).trim() === '') {
      return null;
    }
    const n = Number(String(v).trim().replace(',', '.'));
    return Number.isFinite(n) ? null : { numero: true };
  };

  opcionesLista(c: CampoFormularioDTO): string[] {
    try {
      const arr = JSON.parse(c.opciones || '[]') as unknown;
      return Array.isArray(arr) ? arr.map(String) : [];
    } catch {
      return [];
    }
  }

  buildPayload(): RespuestaCampoDTO[] {
    const out: RespuestaCampoDTO[] = [];
    if (!this.campos?.length) {
      return out;
    }
    for (let i = 0; i < this.campos.length; i++) {
      const c = this.campos[i];
      if (c.idCampo == null) {
        continue;
      }
      const ctrl = this.form.get(this.key(c, i));
      let raw = ctrl?.value;
      if (raw === null || raw === undefined) {
        raw = '';
      }
      if (c.tipoDato === 'CHECKBOX') {
        const s = String(raw).trim();
        if (s === '') {
          continue;
        }
        const low = s.toLowerCase();
        const valor =
          low === 'true' || low === '1' || low === 'sí' || low === 'si' ? 'true' : 'false';
        out.push({ idCampo: c.idCampo, valor });
        continue;
      }
      const str = String(raw).trim();
      if (str === '') {
        continue;
      }
      out.push({ idCampo: c.idCampo, valor: str });
    }
    return out;
  }

  markTouched(): void {
    this.form.markAllAsTouched();
  }

  isValid(): boolean {
    this.markTouched();
    return this.form.valid;
  }
}
