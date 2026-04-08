import { AfterViewInit, ChangeDetectorRef, Component, OnInit, ViewChild, inject } from '@angular/core';
import { CommonModule, NgStyle } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
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
import { RetakeRequest } from '../claim.model';

@Component({
  selector: 'app-retake-requests',
  templateUrl: './retake-requests.html',
  imports: [
    CommonModule,
    NgStyle,
    FormsModule,
    MatButtonModule,
    MatCardModule,
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
  ],
})
export class RetakeRequests implements OnInit, AfterViewInit {
  private readonly claimService = inject(ClaimService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly authService = inject(AuthService);

  isAdmin = false;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  dataSource = new MatTableDataSource<RetakeRequest>();
  loading = true;
  displayedColumns = ['courseName', 'status', 'requestDate', 'actions'];

  searchQuery = '';
  selectedStatus = '';

  readonly statusOptions = [
    { value: '',         label: 'All' },
    { value: 'PENDING',  label: 'Pending' },
    { value: 'APPROVED', label: 'Approved' },
    { value: 'DENIED',   label: 'Denied' },
  ];

  readonly statusLabels: Record<string, string> = {
    PENDING: 'Pending Review',
    APPROVED: 'Approved',
    DENIED: 'Denied',
  };

  ngOnInit() {
    this.authService.user().subscribe(user => {
      this.isAdmin = user.roles?.includes('ADMIN') ?? false;
      this.cdr.markForCheck();
    });
    this.loadRequests();
    this.dataSource.filterPredicate = (data: RetakeRequest, filter: string) => {
      const { search, status } = JSON.parse(filter);
      const matchSearch = !search ||
        data.courseName.toLowerCase().includes(search) ||
        (data.reason?.toLowerCase().includes(search) ?? false);
      const matchStatus = !status || data.status === status;
      return matchSearch && matchStatus;
    };
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  loadRequests() {
    this.loading = true;
    this.claimService.getAllRetakeRequests().subscribe({
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
    });
    if (this.dataSource.paginator) this.dataSource.paginator.firstPage();
  }

  setStatus(value: string) {
    this.selectedStatus = value;
    this.applyFilters();
  }

  clearFilters() {
    this.searchQuery = '';
    this.selectedStatus = '';
    this.applyFilters();
  }

  get hasActiveFilters(): boolean {
    return !!(this.searchQuery || this.selectedStatus);
  }

  updateStatus(id: number | undefined, status: 'APPROVED' | 'DENIED') {
    if (!id) return;
    const action$ = status === 'APPROVED'
      ? this.claimService.approveRetakeRequest(id)
      : this.claimService.denyRetakeRequest(id);

    action$.subscribe({
      next: () => {
        this.snackBar.open(
          status === 'APPROVED' ? 'Request approved successfully.' : 'Request denied.',
          'Close', { duration: 3000 }
        );
        this.loadRequests();
      },
      error: () => {
        this.snackBar.open('Failed to update the request.', 'Close', { duration: 3000 });
        this.cdr.markForCheck();
      },
    });
  }

  statusStyle(status: string): Record<string, string> {
    const map: Record<string, Record<string, string>> = {
      PENDING:  { background: '#fef3c7', color: '#d97706', border: '1px solid #fcd34d' },
      APPROVED: { background: '#d1fae5', color: '#059669', border: '1px solid #6ee7b7' },
      DENIED:   { background: '#fee2e2', color: '#dc2626', border: '1px solid #fca5a5' },
    };
    return map[status] ?? { background: '#f3f4f6', color: '#6b7280', border: '1px solid #d1d5db' };
  }
}
