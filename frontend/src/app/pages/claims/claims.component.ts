import { Component, OnInit, ViewChild, TemplateRef } from '@angular/core';
import { Router } from '@angular/router';
import { ClaimService } from '../../core/services/claim.service';
import { Claim } from '../../core/models/claim.model';
import { TableModel, TableItem, TableHeaderItem, NotificationService } from 'carbon-components-angular';

@Component({
  selector: 'app-claims',
  templateUrl: './claims.component.html',
  styleUrls: ['./claims.component.scss']
})
export class ClaimsComponent implements OnInit {

  @ViewChild('statusTemplate', { static: true }) statusTemplate!: TemplateRef<any>;
  @ViewChild('actionTemplate', { static: true }) actionTemplate!: TemplateRef<any>;
  @ViewChild('subjectTemplate', { static: true }) subjectTemplate!: TemplateRef<any>;

  public model = new TableModel();
  public claimTypes: string[] = [];
  
  // PAGINATION STATE
  public allClaimsData: any[][] = [];
  public itemsPerPage = 10;
  
  // UI STATE
  public isAdding = false;
  public isEditing = false;
  
  // FORM MODEL
  public currentClaim: any = {
    id: null,
    subject: '',
    description: '',
    type: 'PEDAGOGICAL',
    status: 'OPEN',
    studentId: 1 
  };

  constructor(
    private claimService: ClaimService,
    private router: Router,
    private notificationService: NotificationService
  ) { }

  ngOnInit(): void {
    this.model.header = [
      new TableHeaderItem({ data: 'Subject' }),
      new TableHeaderItem({ data: 'Type' }),
      new TableHeaderItem({ data: 'Status' }),
      new TableHeaderItem({ data: 'Actions' })
    ];
    this.loadClaimTypes();
    this.loadClaims();
  }

  loadClaims(): void {
    this.claimService.getAllClaims().subscribe({
      next: (data: Claim[]) => {
        // Store ALL mapped rows in our master array
        this.allClaimsData = data.map(claim => [
          new TableItem({ data: claim, template: this.subjectTemplate }),
          new TableItem({ data: claim.type }),
          new TableItem({ data: claim.status, template: this.statusTemplate }),
          new TableItem({ data: claim, template: this.actionTemplate })
        ]);

        // Setup IBM Carbon Pagination Model
        this.model.totalDataLength = this.allClaimsData.length;
        this.model.pageLength = this.itemsPerPage;
        
        // Render the first page instantly
        this.selectPage(1);
      },
      error: () => this.showNotification('error', 'Error', 'Failed to load claims.')
    });
  }

  // PAGINATION LOGIC
  selectPage(page: number): void {
    this.model.currentPage = page;
    const startIndex = (page - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    
    // Slice only the 10 rows needed for the current view
    this.model.data = this.allClaimsData.slice(startIndex, endIndex);
  }

  loadClaimTypes(): void {
    this.claimService.getClaimTypes().subscribe(types => this.claimTypes = types);
  }

  // --- NAVIGATION & FORMS ---
  viewDetail(id: number): void {
    this.router.navigate(['/app/claims', id]);
  }

  onEdit(claim: Claim): void {
    this.currentClaim = { ...claim, studentId: 1 };
    this.isEditing = true;
    this.isAdding = true; 
  }

  submitClaim(): void {
    if (this.isEditing && this.currentClaim.id) {
      this.claimService.updateClaim(this.currentClaim.id, this.currentClaim).subscribe({
        next: () => {
          this.showNotification('success', 'Updated', 'Claim updated successfully.');
          this.resetForm();
          this.loadClaims();
        },
        error: (err) => this.showNotification('error', 'Error', err.error?.message || 'Update failed.')
      });
    } else {
      this.claimService.createClaim(this.currentClaim).subscribe({
        next: () => {
          this.showNotification('success', 'Success', 'Claim submitted successfully.');
          this.resetForm();
          this.loadClaims();
        },
        error: (err) => this.showNotification('error', 'Error', err.error?.message || 'Submission failed.')
      });
    }
  }

  resetForm(): void {
    this.isAdding = false;
    this.isEditing = false;
    this.currentClaim = {
      id: null,
      subject: '',
      description: '',
      type: this.claimTypes.length > 0 ? this.claimTypes[0] : 'PEDAGOGICAL',
      status: 'OPEN',
      studentId: 1
    };
  }

  // --- ADMIN & WORKFLOW ACTIONS ---
  onAuthorizeRetake(id: number): void {
    if (confirm('Authorize a retake for this claim?')) {
      this.claimService.authorizeRetake(id).subscribe({
        next: () => {
          this.showNotification('success', 'Authorized', 'The student can now submit a retake form.');
          this.loadClaims();
        },
        error: (err) => this.showNotification('error', 'Denied', err.error || 'Could not authorize.')
      });
    }
  }

  onDelete(id: number): void {
    if (confirm('Are you sure you want to delete this claim?')) {
      this.claimService.deleteClaim(id).subscribe({
        next: () => {
          this.showNotification('success', 'Deleted', 'Claim removed successfully.');
          this.loadClaims();
        },
        error: (err) => this.showNotification('error', 'Denied', err.error?.message || 'Cannot delete this claim.')
      });
    }
  }

  showNotification(type: any, title: string, message: string): void {
    this.notificationService.showNotification({
      type, title, message, target: '.notification-container', smart: true
    });
  }
}