import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { RolDTO } from '../Models/rol-dto';
import { CrearRolDTO } from '../Models/crear-rol-dto';

@Injectable({
  providedIn: 'root',
})
export class RolService {
  private apiUrl = `${environment.API_URL}roles`;

  constructor(private http: HttpClient) {}

  listarRoles(): Observable<RolDTO[]> {
    return this.http.get<RolDTO[]>(this.apiUrl);
  }

  /** GET /api/roles/listar — solo administración (token admin). */
  listarParaAdministracion(): Observable<RolDTO[]> {
    return this.http.get<RolDTO[]>(`${this.apiUrl}/listar`);
  }

  /** GET /api/roles/registro — público, solo rol estudiante. */
  listarRolesRegistro(): Observable<RolDTO[]> {
    return this.http.get<RolDTO[]>(`${this.apiUrl}/registro`);
  }

  listarTodos(): Observable<RolDTO[]> {
    return this.http.get<RolDTO[]>(`${this.apiUrl}/todos`);
  }

  crearRol(rol: CrearRolDTO): Observable<RolDTO> {
    return this.http.post<RolDTO>(this.apiUrl, rol);
  }

  actualizarRol(id: number, rol: CrearRolDTO): Observable<RolDTO> {
    return this.http.put<RolDTO>(`${this.apiUrl}/${id}`, rol);
  }

  inactivarRol(id: number): Observable<string> {
    return this.http.put(`${this.apiUrl}/${id}/inactivar`, {}, { responseType: 'text' });
  }
}
