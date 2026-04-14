import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ClaimService } from './claim.service';
import { Claim, RetakeRequest } from '../models/claim.model';

const CLAIM_API = 'http://localhost:8080/api';
const RETAKE_API = 'http://localhost:8080/api';

describe('ClaimService', () => {
  let service: ClaimService;
  let httpMock: HttpTestingController;

  const mockClaim: Claim = {
    id: 1,
    subject: 'Exam contestation',
    description: 'Wrong grade',
    type: 'PEDAGOGICAL',
    status: 'OPEN',
  };

  const mockRetake: RetakeRequest = {
    id: 10,
    courseName: 'Math',
    reason: 'Medical issue',
    status: 'PENDING',
    claimId: 1,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ClaimService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });
    service = TestBed.inject(ClaimService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  // ── getAllClaims ──────────────────────────────────────────────────────────

  it('should fetch all claims without studentId', () => {
    service.getAllClaims().subscribe(claims => {
      expect(claims.length).toBe(1);
      expect(claims[0].subject).toBe('Exam contestation');
    });

    const req = httpMock.expectOne(`${CLAIM_API}/claims`);
    expect(req.request.method).toBe('GET');
    req.flush([mockClaim]);
  });

  it('should fetch claims filtered by studentId', () => {
    service.getAllClaims(42).subscribe();

    const req = httpMock.expectOne(`${CLAIM_API}/claims?studentId=42`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  // ── createClaim ───────────────────────────────────────────────────────────

  it('should create a claim via POST', () => {
    const newClaim: Partial<Claim> = { subject: 'New claim', description: 'Desc', type: 'TECHNICAL' };

    service.createClaim(newClaim).subscribe(result => {
      expect(result.id).toBe(1);
      expect(result.status).toBe('OPEN');
    });

    const req = httpMock.expectOne(`${CLAIM_API}/claims`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(newClaim);
    req.flush(mockClaim);
  });

  // ── deleteClaim ───────────────────────────────────────────────────────────

  it('should delete a claim via DELETE', () => {
    service.deleteClaim(1).subscribe();

    const req = httpMock.expectOne(`${CLAIM_API}/claims/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  // ── authorizeRetake ───────────────────────────────────────────────────────

  it('should authorize a retake via PATCH', () => {
    service.authorizeRetake(1).subscribe(result => {
      expect(result.status).toBe('RETAKE_AUTHORIZED');
    });

    const req = httpMock.expectOne(`${CLAIM_API}/claims/1/authorize-retake`);
    expect(req.request.method).toBe('PATCH');
    req.flush({ ...mockClaim, status: 'RETAKE_AUTHORIZED' });
  });

  // ── approveRetakeRequest ──────────────────────────────────────────────────

  it('should approve a retake request via PATCH', () => {
    service.approveRetakeRequest(10).subscribe(result => {
      expect(result.status).toBe('APPROVED');
    });

    const req = httpMock.expectOne(`${RETAKE_API}/retake-requests/10/approve`);
    expect(req.request.method).toBe('PATCH');
    req.flush({ ...mockRetake, status: 'APPROVED' });
  });

  // ── denyRetakeRequest ─────────────────────────────────────────────────────

  it('should deny a retake request via PATCH', () => {
    service.denyRetakeRequest(10).subscribe(result => {
      expect(result.status).toBe('REJECTED');
    });

    const req = httpMock.expectOne(`${RETAKE_API}/retake-requests/10/deny`);
    expect(req.request.method).toBe('PATCH');
    req.flush({ ...mockRetake, status: 'REJECTED' });
  });

  // ── validateDocument ──────────────────────────────────────────────────────

  it('should validate a document via PATCH', () => {
    service.validateDocument(10).subscribe(result => {
      expect(result.documentStatus).toBe('VALID');
    });

    const req = httpMock.expectOne(`${RETAKE_API}/retake-requests/10/validate-document`);
    expect(req.request.method).toBe('PATCH');
    req.flush({ ...mockRetake, documentStatus: 'VALID' });
  });

  // ── getAttachmentUrl ──────────────────────────────────────────────────────

  it('should build correct attachment URL from path', () => {
    const url = service.getAttachmentUrl('/uploads/doc.pdf');
    expect(url).toContain('doc.pdf');
    expect(url).toContain(`${RETAKE_API}/retake-requests/attachments/`);
  });

  it('should build correct attachment URL from Windows-style path', () => {
    const url = service.getAttachmentUrl('C:\\uploads\\doc.pdf');
    expect(url).toContain('doc.pdf');
  });
});