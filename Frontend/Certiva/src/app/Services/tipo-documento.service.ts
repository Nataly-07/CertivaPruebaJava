import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { TipoDocumentoDTO } from '../Models/tipo-documento-dto';

@Injectable({
  providedIn: 'root',
})
export class TipoDocumentoService {
  private apiUrl = `${environment.API_URL}tipo-documentos`;

  constructor(private http: HttpClient) {}

  listar(): Observable<TipoDocumentoDTO[]> {
    return this.http.get<TipoDocumentoDTO[]>(this.apiUrl);
  }
}
