import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment'; 

@Injectable({
  providedIn: 'root'
})
export class ClaimService {

  private apiUrl = `${environment.apiUrl}/claims`;
  // New base URL for Retake Requests
  private retakeUrl = `${environment.apiUrl}/retake-requests`;

  constructor(private http: HttpClient) { }

  // --- CLAIM METHODS ---

  getAllClaims(): Observable<any> {
    return this.http.get(this.apiUrl);
  }

  getClaimById(id: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/${id}`);
  }

  createClaim(claim: any): Observable<any> {
    return this.http.post(this.apiUrl, claim);
  }

  updateClaim(id: number, claim: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, claim);
  }

  deleteClaim(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  getClaimTypes(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/types`);
  }

  // --- RETAKE REQUEST METHODS ---

  createRetakeFromClaim(claimId: number, retakeData: any, file: File): Observable<any> {
    const formData: FormData = new FormData();

    if (file) {
      formData.append('file', file);
    }
    formData.append('request', JSON.stringify(retakeData));

    return this.http.post(`${this.retakeUrl}/from-claim/${claimId}`, formData);
  }

  // Fetch all retake requests for a future "Retake List" page
  getAllRetakeRequests(): Observable<any[]> {
    return this.http.get<any[]>(this.retakeUrl);
  }

  authorizeRetake(id: number) {
    return this.http.patch(`${this.apiUrl}/${id}/authorize-retake`, {});
  }
}