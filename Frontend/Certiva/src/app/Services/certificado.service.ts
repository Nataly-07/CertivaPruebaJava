import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CertificadoDTO, CertificadoVerificacionDTO, CertificadosAdminVistaDTO, CertificadoAdminFilaDTO } from '../Models/certificado-dto';
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

  obtenerVistaAdmin(busqueda?: string, idEvento?: number | null): Observable<CertificadosAdminVistaDTO> {
    const params: Record<string, string> = {};
    if (busqueda?.trim()) {
      params['busqueda'] = busqueda.trim();
    }
    if (idEvento != null && idEvento > 0) {
      params['idEvento'] = String(idEvento);
    }
    return this.http.get<CertificadosAdminVistaDTO>(`${this.baseUrl}/admin/vista`, { params });
  }

  descargarPdfAdmin(idCertificado: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/admin/${idCertificado}/pdf`, { responseType: 'blob' });
  }

  revocarAdmin(idCertificado: number): Observable<CertificadoAdminFilaDTO> {
    return this.http.post<CertificadoAdminFilaDTO>(`${this.baseUrl}/admin/${idCertificado}/revocar`, {});
  }
}
