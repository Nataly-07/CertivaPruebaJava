import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { DashboardActivityDTO, DashboardDTO } from '../Models/dashboard-dto';

@Injectable({
  providedIn: 'root',
})
export class DashboardService {
  private readonly baseUrl = `${environment.API_URL}dashboard`;

  constructor(private http: HttpClient) {}

  obtenerEstadisticas(): Observable<DashboardDTO> {
    return this.http.get<DashboardDTO>(`${this.baseUrl}/stats`);
  }

  obtenerActividad(rangoDias: 7 | 30 | 90): Observable<DashboardActivityDTO> {
    const params = new HttpParams().set('rango', String(rangoDias));
    return this.http.get<DashboardActivityDTO>(`${this.baseUrl}/activity`, { params });
  }
}
