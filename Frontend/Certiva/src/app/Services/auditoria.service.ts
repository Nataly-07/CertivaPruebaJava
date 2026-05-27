import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuditoriaResumenDTO } from '../Models/auditoria-dto';

export interface AuditoriaFiltros {
  limite?: number;
  accion?: string;
  desde?: string;
  hasta?: string;
  busqueda?: string;
}

@Injectable({
  providedIn: 'root',
})
export class AuditoriaService {
  private readonly baseUrl = `${environment.API_URL}auditoria`;

  constructor(private http: HttpClient) {}

  listarRecientes(filtros: AuditoriaFiltros = {}): Observable<AuditoriaResumenDTO[]> {
    let params = new HttpParams().set('limite', String(filtros.limite ?? 200));
    if (filtros.accion?.trim()) {
      params = params.set('accion', filtros.accion.trim());
    }
    if (filtros.desde) {
      params = params.set('desde', filtros.desde);
    }
    if (filtros.hasta) {
      params = params.set('hasta', filtros.hasta);
    }
    if (filtros.busqueda?.trim()) {
      params = params.set('busqueda', filtros.busqueda.trim());
    }
    return this.http.get<AuditoriaResumenDTO[]>(`${this.baseUrl}/recientes`, { params });
  }
}
