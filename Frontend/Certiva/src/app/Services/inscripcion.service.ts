import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CrearInscripcionDTO } from '../Models/inscripcion-dto';
import { CheckInRespuestaDTO } from '../Models/certificado-dto';
import { InscripcionPortalDTO } from '../Models/portal-dto';
import { normalizarCodigoQr } from '../utils/inscripcion-qr';

@Injectable({
  providedIn: 'root',
})
export class InscripcionService {
  private readonly baseUrl = `${environment.API_URL}inscripciones`;

  constructor(private http: HttpClient) {}

  crear(dto: CrearInscripcionDTO): Observable<CrearInscripcionDTO> {
    return this.http.post<CrearInscripcionDTO>(this.baseUrl, dto);
  }

  checkIn(codigoQr: string): Observable<CheckInRespuestaDTO> {
    const codigo = normalizarCodigoQr(codigoQr);
    return this.http.post<CheckInRespuestaDTO>(`${environment.API_URL}check-in`, { codigo });
  }

  /** Valida asistencia por id de inscripción (mismo flujo que el QR escaneado). */
  validarAsistencia(inscripcionId: number): Observable<CheckInRespuestaDTO> {
    return this.http.post<CheckInRespuestaDTO>(
      `${environment.API_URL}asistencias/validar`,
      null,
      { params: { inscripcionId: String(inscripcionId) } }
    );
  }

  listarMis(): Observable<InscripcionPortalDTO[]> {
    return this.http.get<InscripcionPortalDTO[]>(`${this.baseUrl}/mis`);
  }
}
