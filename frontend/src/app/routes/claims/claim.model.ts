export interface RetakeRequest {
  id?: number;
  courseName: string;
  reason: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  attachmentPath?: string;
  claimId?: number;
  studentId?: number;
  requestDate?: string;
}

export interface Student {
  id: number;
  firstName?: string;
  lastName?: string;
  email?: string;
}

export interface Claim {
  id?: number;
  subject: string;
  description: string;
  type: 'TECHNICAL' | 'PEDAGOGICAL' | 'ADMINISTRATIVE' | 'OTHER';
  status: 'OPEN' | 'IN_PROGRESS' | 'RETAKE_AUTHORIZED' | 'RESOLVED' | 'REJECTED' | 'CANCELED';
  createdAt?: string;
  student?: Student;
  retakeRequest?: RetakeRequest;
  retakeRequestId?: number;
}
