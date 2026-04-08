import { AfterViewInit, ChangeDetectorRef, Component, OnInit, ViewChild, inject } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule, NgStyle } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { PageHeader } from '@shared';
import { AuthService } from '@core/authentication';
import { ClaimService } from '../claim.service';
import { Claim } from '../claim.model';
import { NewClaimDialog } from './new-claim-dialog';

@Component({
  selector: 'app-claims-list',
  templateUrl: './claims-list.html',
  imports: [
    CommonModule,
    NgStyle,
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatSortModule,
    MatTableModule,
    MatTooltipModule,
    PageHeader,
    NewClaimDialog,
  ],
})
export class ClaimsList implements OnInit, AfterViewInit {
  private readonly claimService = inject(ClaimService);
  private readonly router = inject(Router);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly authService = inject(AuthService);

  isAdmin = false;
  currentUserId: number | undefined;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  dataSource = new MatTableDataSource<Claim>();
  loading = true;
  displayedColumns = ['subject', 'type', 'status', 'createdAt', 'actions'];

  searchQuery = '';
  selectedStatus = '';
  selectedType = '';

  readonly statusOptions = [
    { value: '',                  label: 'All Statuses' },
    { value: 'OPEN',              label: 'Open' },
    { value: 'IN_PROGRESS',       label: 'In Progress' },
    { value: 'RETAKE_AUTHORIZED', label: 'Retake Authorized' },
    { value: 'RESOLVED',          label: 'Resolved' },
    { value: 'REJECTED',          label: 'Rejected' },
    { value: 'CANCELED',          label: 'Canceled' },
  ];

  readonly typeOptions = [
    { value: '',               label: 'All Types' },
    { value: 'TECHNICAL',      label: 'Technical' },
    { value: 'PEDAGOGICAL',    label: 'Pedagogical' },
    { value: 'ADMINISTRATIVE', label: 'Administrative' },
    { value: 'OTHER',          label: 'Other' },
  ];

  readonly statusLabels: Record<string, string> = {
    OPEN: 'Open', IN_PROGRESS: 'In Progress', RETAKE_AUTHORIZED: 'Retake Authorized',
    RESOLVED: 'Resolved', REJECTED: 'Rejected', CANCELED: 'Canceled',
  };

  readonly typeLabels: Record<string, string> = {
    TECHNICAL: 'Technical', PEDAGOGICAL: 'Pedagogical',
    ADMINISTRATIVE: 'Administrative', OTHER: 'Other',
  };

  ngOnInit() {
    this.authService.user().subscribe(user => {
      this.isAdmin = user.roles?.includes('ADMIN') ?? false;
      this.currentUserId = user.id ? Number(user.id) : undefined;
      this.loadClaims();
      this.cdr.markForCheck();
    });
    this.dataSource.filterPredicate = (data: Claim, filter: string) => {
      const { search, status, type } = JSON.parse(filter);
      const matchSearch = !search ||
        data.subject.toLowerCase().includes(search) ||
        (data.description?.toLowerCase().includes(search) ?? false);
      const matchStatus = !status || data.status === status;
      const matchType = !type || data.type === type;
      return matchSearch && matchStatus && matchType;
    };
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  loadClaims() {
    this.loading = true;
    const studentId = this.isAdmin ? undefined : this.currentUserId;
    this.claimService.getAllClaims(studentId).subscribe({
      next: data => {
        this.dataSource.data = data;
        this.loading = false;
        this.cdr.markForCheck();
        setTimeout(() => {
          this.dataSource.paginator = this.paginator;
          this.dataSource.sort = this.sort;
        });
      },
      error: () => {
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
  }

  applyFilters() {
    this.dataSource.filter = JSON.stringify({
      search: this.searchQuery.toLowerCase().trim(),
      status: this.selectedStatus,
      type: this.selectedType,
    });
    if (this.dataSource.paginator) this.dataSource.paginator.firstPage();
  }

  setStatus(value: string) {
    this.selectedStatus = value;
    this.applyFilters();
  }

  setType(value: string) {
    this.selectedType = value;
    this.applyFilters();
  }

  clearFilters() {
    this.searchQuery = '';
    this.selectedStatus = '';
    this.selectedType = '';
    this.applyFilters();
  }

  get hasActiveFilters(): boolean {
    return !!(this.searchQuery || this.selectedStatus || this.selectedType);
  }

  openNewClaimDialog() {
    const ref = this.dialog.open(NewClaimDialog, { width: '520px' });
    ref.afterClosed().subscribe(result => {
      if (result) this.loadClaims();
    });
  }

  viewDetail(id: number | undefined) {
    if (id) this.router.navigate(['/claims', id]);
  }

  deleteClaim(id: number | undefined) {
    if (!id) return;
    const sb = this.snackBar.open('Delete this claim?', 'Yes, Delete', {
      duration: 5000,
      panelClass: 'confirm-snack',
    });
    sb.onAction().subscribe(() => {
      this.claimService.deleteClaim(id).subscribe({
        next: () => {
          this.snackBar.open('Claim deleted.', 'Close', { duration: 3000 });
          this.loadClaims();
        },
        error: () => this.snackBar.open('Could not delete this claim.', 'Close', { duration: 3000 }),
      });
    });
  }

  statusStyle(status: string): Record<string, string> {
    const map: Record<string, Record<string, string>> = {
      OPEN:              { background: '#dbeafe', color: '#1d4ed8', border: '1px solid #93c5fd' },
      IN_PROGRESS:       { background: '#fef3c7', color: '#d97706', border: '1px solid #fcd34d' },
      RETAKE_AUTHORIZED: { background: '#ede9fe', color: '#6d28d9', border: '1px solid #c4b5fd' },
      RESOLVED:          { background: '#d1fae5', color: '#059669', border: '1px solid #6ee7b7' },
      REJECTED:          { background: '#fee2e2', color: '#dc2626', border: '1px solid #fca5a5' },
      CANCELED:          { background: '#f3f4f6', color: '#6b7280', border: '1px solid #d1d5db' },
    };
    return map[status] ?? { background: '#f3f4f6', color: '#374151', border: '1px solid #d1d5db' };
  }

  typeStyle(): Record<string, string> {
    return { background: '#f1f5f9', color: '#475569', border: '1px solid #e2e8f0' };
  }
}
