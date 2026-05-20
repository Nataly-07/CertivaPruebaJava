import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuditoriaResumenDTO } from '../Models/auditoria-dto';

@Injectable({
  providedIn: 'root',
})
export class AuditoriaService {
  private readonly baseUrl = `${environment.API_URL}auditoria`;

  constructor(private http: HttpClient) {}

  listarRecientes(limite = 100): Observable<AuditoriaResumenDTO[]> {
    return this.http.get<AuditoriaResumenDTO[]>(`${this.baseUrl}/recientes`, {
      params: { limite: String(limite) },
    });
  }
}
