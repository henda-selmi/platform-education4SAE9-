import { Routes } from '@angular/router';
import { ClaimsList } from './claims-list/claims-list';
import { ClaimDetail } from './claim-detail/claim-detail';
import { RetakeRequests } from './retake-requests/retake-requests';

export const routes: Routes = [
  { path: '', component: ClaimsList },
  { path: 'retake-requests', component: RetakeRequests },
  { path: ':id', component: ClaimDetail },
];
