import { RetakeRequest } from "./retake-request.model";

export interface Claim {
    id?: number;
    subject: string;
    description: string;
    type: string;
    status: string;
    createdAt?: string;
    student: { id: number };
    retakeRequest?: RetakeRequest;
  }