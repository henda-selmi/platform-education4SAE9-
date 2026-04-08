import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ClaimService } from '../claim.service';

@Component({
  selector: 'app-new-claim-dialog',
  template: `
    <h2 mat-dialog-title>Submit a New Claim</h2>
    <mat-dialog-content class="flex flex-col gap-4 pt-2">
      <mat-form-field class="w-full">
        <mat-label>Subject</mat-label>
        <input matInput [(ngModel)]="subject" placeholder="Brief summary of your claim">
      </mat-form-field>

      <mat-form-field class="w-full">
        <mat-label>Type</mat-label>
        <mat-select [(ngModel)]="type">
          <mat-option value="TECHNICAL">Technical</mat-option>
          <mat-option value="PEDAGOGICAL">Pedagogical</mat-option>
          <mat-option value="ADMINISTRATIVE">Administrative</mat-option>
          <mat-option value="OTHER">Other</mat-option>
        </mat-select>
      </mat-form-field>

      <mat-form-field class="w-full">
        <mat-label>Description</mat-label>
        <textarea matInput [(ngModel)]="description" rows="4" placeholder="Describe your issue in detail..."></textarea>
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancel</button>
      <button mat-raised-button color="primary" (click)="submit()" [disabled]="submitting || !subject || !type || !description">
        <mat-spinner *ngIf="submitting" diameter="18" style="display:inline-block;margin-right:6px;"></mat-spinner>
        Submit
      </button>
    </mat-dialog-actions>
  `,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
  ],
})
export class NewClaimDialog {
  private readonly claimService = inject(ClaimService);
  private readonly dialogRef = inject(MatDialogRef<NewClaimDialog>);
  private readonly snackBar = inject(MatSnackBar);

  subject = '';
  type: 'TECHNICAL' | 'PEDAGOGICAL' | 'ADMINISTRATIVE' | 'OTHER' | '' = '';
  description = '';
  submitting = false;

  submit() {
    if (!this.subject || !this.type || !this.description) return;
    this.submitting = true;
    this.claimService
      .createClaim({ subject: this.subject, type: this.type as any, description: this.description, status: 'OPEN' })
      .subscribe({
        next: () => {
          this.snackBar.open('Claim submitted successfully.', 'Close', { duration: 3000 });
          this.dialogRef.close(true);
        },
        error: () => {
          this.snackBar.open('Failed to submit claim.', 'Close', { duration: 3000 });
          this.submitting = false;
        },
      });
  }
}