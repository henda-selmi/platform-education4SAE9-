import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '@env/environment';
import { Claim, Message, RetakeRequest } from './claim.model';

@Injectable({ providedIn: 'root' })
export class ClaimService {
  private readonly http = inject(HttpClient);
  private readonly claimApi = environment.claimApiUrl;
  private readonly retakeApi = environment.retakeApiUrl;

  getAllClaims(studentId?: number) {
    const url = studentId
      ? `${this.claimApi}/claims?studentId=${studentId}`
      : `${this.claimApi}/claims`;
    return this.http.get<Claim[]>(url);
  }

  getClaimById(id: number) {
    return this.http.get<Claim>(`${this.claimApi}/claims/${id}`);
  }

  createClaim(claim: Partial<Claim>) {
    return this.http.post<Claim>(`${this.claimApi}/claims`, claim);
  }

  deleteClaim(id: number) {
    return this.http.delete<void>(`${this.claimApi}/claims/${id}`);
  }

  authorizeRetake(id: number) {
    return this.http.patch<Claim>(`${this.claimApi}/claims/${id}/authorize-retake`, {});
  }

  createRetakeFromClaim(claimId: number, courseName: string, reason: string, file: File) {
    const formData = new FormData();
    const retakeRequest = { courseName, reason, status: 'PENDING' };
    formData.append('retakeRequest', new Blob([JSON.stringify(retakeRequest)], { type: 'application/json' }));
    formData.append('attachment', file);
    return this.http.post<any>(`${this.retakeApi}/retake-requests/from-claim/${claimId}`, formData);
  }

  getRetakeRequestById(id: number) {
    return this.http.get<RetakeRequest>(`${this.retakeApi}/retake-requests/${id}`);
  }

  getAttachmentUrl(attachmentPath: string): string {
    const filename = attachmentPath.split('/').pop() || attachmentPath;
    return `${this.retakeApi}/retake-requests/attachments/${encodeURIComponent(filename)}`;
  }

  downloadAttachment(attachmentPath: string) {
    const url = this.getAttachmentUrl(attachmentPath);
    const filename = attachmentPath.split('/').pop() || attachmentPath;
    this.http.get(url, { responseType: 'blob' }).subscribe((blob: Blob) => {
      const blobUrl = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = blobUrl;
      a.download = filename;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(blobUrl);
    });
  }

  getAllRetakeRequests() {
    return this.http.get<RetakeRequest[]>(`${this.retakeApi}/retake-requests`);
  }

  updateRetakeRequest(id: number, data: Partial<RetakeRequest>) {
    return this.http.put<RetakeRequest>(`${this.retakeApi}/retake-requests/${id}`, data);
  }

  approveRetakeRequest(id: number) {
    return this.http.patch<RetakeRequest>(`${this.retakeApi}/retake-requests/${id}/approve`, {});
  }

  denyRetakeRequest(id: number) {
    return this.http.patch<RetakeRequest>(`${this.retakeApi}/retake-requests/${id}/deny`, {});
  }

  validateDocument(retakeId: number) {
    return this.http.patch<RetakeRequest>(`${this.retakeApi}/retake-requests/${retakeId}/validate-document`, {});
  }

  rejectDocument(retakeId: number, reason: string) {
    return this.http.patch<RetakeRequest>(`${this.retakeApi}/retake-requests/${retakeId}/reject-document`, { reason });
  }

  resubmitDocument(retakeId: number, file: File) {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.patch<RetakeRequest>(`${this.retakeApi}/retake-requests/${retakeId}/resubmit-document`, formData);
  }

  getMessages(claimId: number) {
    return this.http.get<Message[]>(`${this.claimApi}/claims/${claimId}/messages`);
  }

  sendMessage(claimId: number, message: Partial<Message>) {
    return this.http.post<Message>(`${this.claimApi}/claims/${claimId}/messages`, message);
  }
}
