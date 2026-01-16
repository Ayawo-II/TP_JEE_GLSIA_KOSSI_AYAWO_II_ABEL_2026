import { HttpClient, HttpHeaders } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

const BASE_URL = 'http://localhost:8080';

@Injectable({
  providedIn: 'root',
})
export class TransactionService {
  http = inject(HttpClient);

  // DÉPÔT
  effectuerDepot(depotData: any): Observable<any> {
    const headers = this.getHeaders();
    return this.http.post(`${BASE_URL}/transaction`, depotData, {headers});
  }

  // RETRAIT
  effectuerRetrait(retraitData: any): Observable<any> {
    const headers = this.getHeaders();
    return this.http.post(`${BASE_URL}/transaction`, retraitData, {headers});
  }

  // VIREMENT
  effectuerVirement(virementData: {
    montant: number,
    numeroCompteSource: string,
    numeroCompteDestination: string
  }): Observable<any> {
    const headers = this.getHeaders();
    return this.http.post(`${BASE_URL}/transaction`, virementData, {headers});
  }

  // HEADERS
  private getHeaders(): HttpHeaders {
    return new HttpHeaders({
      "Authorization": "Bearer " + localStorage.getItem("token"),
      "Content-Type": "application/json"
    });
  }
  
}