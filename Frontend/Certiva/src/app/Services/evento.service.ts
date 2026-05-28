import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  CampoFormularioDTO,
  CrearEventoDTO,
  EventoCupoVerificacionDTO,
  EventoDTO,
  EventoFilaAdminDTO,
  EventoCatalogoPublicoDTO,
  EventoPublicoDTO,
  EventoResumenTipoDTO,
  EventoCierreResultadoDTO,
  ModalidadEvento,
  TipoEventoEnum,
} from '../Models/evento-dto';
import {
  AsistenciaManualRequestDTO,
  EventoContenidoAcademicoDTO,
  EventoRevisionPanelDTO,
  GuardarEventoContenidoAcademicoDTO,
  ProfesorPanelDTO,
  ProfesorParticipanteDTO,
} from '../Models/portal-dto';
import type { EventoAsistenciaEnVivoDTO, GuardarRevisionAlumnoDTO } from '../Models/portal-dto';
import { EventoPublico } from '../Models/evento-publico';

export interface ListarEventosFiltros {
  soloActivos?: boolean;
  modalidad?: ModalidadEvento;
  tipo?: TipoEventoEnum;
  desde?: string;
  hasta?: string;
  estadoOperativo?: string;
}

@Injectable({
  providedIn: 'root',
})
export class EventoService {
  private readonly baseUrl = `${environment.API_URL}eventos`;

  constructor(private http: HttpClient) {}

  resumenTipos(filtros?: Pick<ListarEventosFiltros, 'soloActivos' | 'modalidad'>): Observable<EventoResumenTipoDTO[]> {
    let params = new HttpParams();
    if (filtros?.soloActivos !== undefined) {
      params = params.set('soloActivos', String(filtros.soloActivos));
    }
    if (filtros?.modalidad) {
      params = params.set('modalidad', filtros.modalidad);
    }
    return this.http.get<EventoResumenTipoDTO[]>(`${this.baseUrl}/resumen-tipos`, { params });
  }

  vistaAdmin(filtros?: ListarEventosFiltros): Observable<EventoFilaAdminDTO[]> {
    let params = new HttpParams();
    if (filtros?.soloActivos !== undefined) {
      params = params.set('soloActivos', String(filtros.soloActivos));
    }
    if (filtros?.modalidad) {
      params = params.set('modalidad', filtros.modalidad);
    }
    if (filtros?.tipo) {
      params = params.set('tipo', filtros.tipo);
    }
    if (filtros?.desde) {
      params = params.set('desde', filtros.desde);
    }
    if (filtros?.hasta) {
      params = params.set('hasta', filtros.hasta);
    }
    if (filtros?.estadoOperativo) {
      params = params.set('estadoOperativo', filtros.estadoOperativo);
    }
    return this.http.get<EventoFilaAdminDTO[]>(`${this.baseUrl}/vista-admin`, { params });
  }

  listar(filtros?: ListarEventosFiltros): Observable<EventoDTO[]> {
    let params = new HttpParams();
    if (filtros?.soloActivos !== undefined) {
      params = params.set('soloActivos', String(filtros.soloActivos));
    }
    if (filtros?.modalidad) {
      params = params.set('modalidad', filtros.modalidad);
    }
    if (filtros?.tipo) {
      params = params.set('tipo', filtros.tipo);
    }
    if (filtros?.desde) {
      params = params.set('desde', filtros.desde);
    }
    if (filtros?.hasta) {
      params = params.set('hasta', filtros.hasta);
    }
    return this.http.get<EventoDTO[]>(this.baseUrl, { params });
  }

  obtener(id: number): Observable<EventoDTO> {
    return this.http.get<EventoDTO>(`${this.baseUrl}/${id}`);
  }

  verificarCupo(id: number): Observable<EventoCupoVerificacionDTO> {
    return this.http.get<EventoCupoVerificacionDTO>(`${this.baseUrl}/${id}/verificar-cupo`);
  }

  getCamposEvento(id: number): Observable<CampoFormularioDTO[]> {
    return this.http.get<CampoFormularioDTO[]>(`${this.baseUrl}/${id}/campos`);
  }

  crear(dto: CrearEventoDTO, imagen?: File | null, pensum?: File | null): Observable<EventoDTO> {
    const form = new FormData();
    form.append('evento', new Blob([JSON.stringify(dto)], { type: 'application/json' }));
    if (imagen) {
      form.append('imagen', imagen);
    }
    if (pensum) {
      form.append('pensum', pensum);
    }
    return this.http.post<EventoDTO>(this.baseUrl, form);
  }

  actualizar(id: number, dto: EventoDTO): Observable<EventoDTO> {
    return this.http.put<EventoDTO>(`${this.baseUrl}/${id}`, dto);
  }

  inactivar(id: number): Observable<string> {
    return this.http.put(`${this.baseUrl}/${id}/inactivar`, {}, { responseType: 'text' });
  }

  eliminarLogico(id: number): Observable<string> {
    return this.http.delete(`${this.baseUrl}/${id}`, { responseType: 'text' });
  }

  /** Catálogo activo (público, sin autenticación). */
  listarCatalogoPublico(): Observable<EventoCatalogoPublicoDTO[]> {
    return this.obtenerCatalogoPublico();
  }

  /** GET /api/public/eventos/catalogo — propiedades alineadas con el JSON del backend. */
  obtenerCatalogoPublico(): Observable<EventoPublico[]> {
    const base = environment.API_URL.replace(/\/$/, '');
    return this.http.get<EventoPublico[]>(`${base}/public/eventos/catalogo`);
  }

  obtenerPublicoPorCodigoDifusion(codigo: string): Observable<EventoPublicoDTO> {
    const base = environment.API_URL.replace(/\/$/, '');
    return this.http.get<EventoPublicoDTO>(`${base}/public/eventos/difusion/${encodeURIComponent(codigo)}`);
  }

  obtenerPanelProfesor(): Observable<ProfesorPanelDTO> {
    return this.http.get<ProfesorPanelDTO>(`${this.baseUrl}/mi-panel`);
  }

  obtenerRevisionCierre(idEvento: number): Observable<EventoRevisionPanelDTO> {
    return this.http.get<EventoRevisionPanelDTO>(`${this.baseUrl}/mi-panel/revision/${idEvento}`);
  }

  obtenerAsistenciaEnVivo(idEvento: number): Observable<EventoAsistenciaEnVivoDTO> {
    return this.http.get<EventoAsistenciaEnVivoDTO>(`${this.baseUrl}/mi-panel/${idEvento}/asistencia-en-vivo`);
  }

  obtenerMatrizAsistencia(idEvento: number): Observable<EventoAsistenciaEnVivoDTO> {
    const base = environment.API_URL.replace(/\/$/, '');
    return this.http.get<EventoAsistenciaEnVivoDTO>(`${base}/events/${idEvento}/attendance-matrix`);
  }

  registrarAsistenciaManual(payload: AsistenciaManualRequestDTO): Observable<{ mensaje: string; idInscripcion: number }> {
    const base = environment.API_URL.replace(/\/$/, '');
    return this.http.post<{ mensaje: string; idInscripcion: number }>(`${base}/attendance/manual-checkin`, payload);
  }

  guardarEvaluacionesRevision(
    idEvento: number,
    alumnos: GuardarRevisionAlumnoDTO[]
  ): Observable<EventoRevisionPanelDTO> {
    return this.http.put<EventoRevisionPanelDTO>(`${this.baseUrl}/mi-panel/revision/${idEvento}/evaluaciones`, {
      alumnos,
    });
  }

  obtenerContenidoAcademico(idEvento: number): Observable<EventoContenidoAcademicoDTO> {
    return this.http.get<EventoContenidoAcademicoDTO>(`${this.baseUrl}/mi-panel/${idEvento}/contenido-academico`);
  }

  guardarContenidoAcademico(
    idEvento: number,
    payload: GuardarEventoContenidoAcademicoDTO
  ): Observable<EventoContenidoAcademicoDTO> {
    return this.http.put<EventoContenidoAcademicoDTO>(`${this.baseUrl}/mi-panel/${idEvento}/contenido-academico`, payload);
  }

  listarParticipantesAsignados(idEvento: number): Observable<ProfesorParticipanteDTO[]> {
    return this.http.get<ProfesorParticipanteDTO[]>(`${this.baseUrl}/mi-panel/${idEvento}/participantes`);
  }

  cancelarEvento(id: number): Observable<string> {
    return this.http.post(`${this.baseUrl}/${id}/cancelar`, {}, { responseType: 'text' });
  }

  iniciarRevision(id: number): Observable<string> {
    return this.http.post(`${this.baseUrl}/${id}/iniciar-revision`, {}, { responseType: 'text' });
  }

  cerrarYCertificar(id: number): Observable<EventoCierreResultadoDTO> {
    return this.http.post<EventoCierreResultadoDTO>(`${this.baseUrl}/${id}/cerrar-y-certificar`, {});
  }

  forzarCierre(id: number): Observable<EventoCierreResultadoDTO> {
    return this.http.post<EventoCierreResultadoDTO>(`${this.baseUrl}/${id}/forzar-cierre`, {});
  }

  reasignarStaff(
    id: number,
    payload: { idsProfesoresColaboradores: number[]; idsMonitoresAsignados: number[] }
  ): Observable<EventoDTO> {
    return this.http.patch<EventoDTO>(`${this.baseUrl}/${id}/reasignar-staff`, payload);
  }
}
