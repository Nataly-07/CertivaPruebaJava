import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { UsuarioDTO } from '../Models/usuario-dto';
import { CrearUsuarioDTO } from '../Models/crear-usuario-dto';
import { UsuarioStaffDTO } from '../Models/evento-dto';

import { ImportacionCsvResultadoDTO } from '../Models/importacion-csv-dto';

@Injectable({
  providedIn: 'root',
})
export class UsuarioService {
  private apiUrl = `${environment.API_URL}usuarios`;

  constructor(private http: HttpClient) {}

  registrar(dto: CrearUsuarioDTO): Observable<UsuarioDTO> {
    return this.http.post<UsuarioDTO>(`${this.apiUrl}/registrar`, dto);
  }

  crearDesdeAdministracion(dto: CrearUsuarioDTO): Observable<UsuarioDTO> {
    return this.http.post<UsuarioDTO>(this.apiUrl, dto);
  }

  listar(): Observable<UsuarioDTO[]> {
    return this.http.get<UsuarioDTO[]>(this.apiUrl);
  }

  actualizar(id: number, dto: Partial<UsuarioDTO>): Observable<UsuarioDTO> {
    return this.http.put<UsuarioDTO>(`${this.apiUrl}/${id}`, dto);
  }

  cambiarRol(id: number, idRol: number): Observable<UsuarioDTO> {
    return this.http.patch<UsuarioDTO>(`${this.apiUrl}/${id}/rol`, { idRol });
  }

  inactivar(id: number): Observable<string> {
    return this.http.put(`${this.apiUrl}/${id}/inactivar`, {}, { responseType: 'text' });
  }

  actualizarMiTelefono(telefono: string): Observable<UsuarioDTO> {
    return this.http.patch<UsuarioDTO>(`${this.apiUrl}/mi-perfil/telefono`, { telefono });
  }

  /** Solo ROLE_PROFESOR o ROLE_MONITOR (no incluye estudiantes). */
  buscarStaffPorRol(codigoRol: 'PROFESOR' | 'MONITOR', q?: string): Observable<UsuarioStaffDTO[]> {
    let params = new HttpParams();
    if (q?.trim()) {
      params = params.set('q', q.trim());
    }
    return this.http.get<UsuarioStaffDTO[]>(`${this.apiUrl}/rol/${codigoRol}`, { params });
  }

  importarCsv(archivo: File): Observable<ImportacionCsvResultadoDTO> {
    const form = new FormData();
    form.append('archivo', archivo);
    return this.http.post<ImportacionCsvResultadoDTO>(`${this.apiUrl}/importar-csv`, form);
  }
}
