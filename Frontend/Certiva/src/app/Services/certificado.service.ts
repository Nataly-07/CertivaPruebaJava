import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CertificadoDTO, CertificadoVerificacionDTO } from '../Models/certificado-dto';
import { CertificadoPortalDTO } from '../Models/portal-dto';

@Injectable({
  providedIn: 'root',
})
export class CertificadoService {
  private readonly baseUrl = `${environment.API_URL}certificados`;

  constructor(private http: HttpClient) {}

  listar(): Observable<CertificadoDTO[]> {
    return this.http.get<CertificadoDTO[]>(this.baseUrl);
  }

  verificarPublico(codigo: string): Observable<CertificadoVerificacionDTO> {
    return this.http.get<CertificadoVerificacionDTO>(
      `${this.baseUrl}/verificar/${encodeURIComponent(codigo.trim())}`
    );
  }

  listarMis(): Observable<CertificadoPortalDTO[]> {
    return this.http.get<CertificadoPortalDTO[]>(`${this.baseUrl}/mis`);
  }

  descargarPdfMi(idCertificado: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/mis/${idCertificado}/pdf`, {
      responseType: 'blob',
    });
  }

  emitirPorInscripcion(idInscripcion: number): Observable<CertificadoDTO> {
    return this.http.post<CertificadoDTO>(`${this.baseUrl}/mis/inscripcion/${idInscripcion}/emitir`, {});
  }
}
