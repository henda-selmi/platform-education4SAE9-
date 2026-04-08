import { Routes } from '@angular/router';
import { ClaimsListComponent } from './claims-list/claims-list.component';
import { ClaimDetailComponent } from './claim-detail/claim-detail.component';
import { RetakeRequestsComponent } from './retake-requests/retake-requests.component';

export const routes: Routes = [
  { path: '', component: ClaimsListComponent },
  { path: 'retake-requests', component: RetakeRequestsComponent },
  { path: ':id', component: ClaimDetailComponent },
];
