import { Component, OnInit, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { take } from 'rxjs';
import { ClaimService } from '../../services/claim.service';
import { AuthService, User } from '@core/authentication';

@Component({
  selector: 'app-new-claim-dialog',
  templateUrl: './new-claim-dialog.component.html',
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
export class NewClaimDialogComponent implements OnInit {
  private readonly claimService = inject(ClaimService);
  private readonly authService = inject(AuthService);
  private readonly dialogRef = inject(MatDialogRef<NewClaimDialogComponent>);
  private readonly snackBar = inject(MatSnackBar);
  private readonly cdr = inject(ChangeDetectorRef);

  currentUser: User | null = null;
  subject = '';
  type: 'TECHNICAL' | 'PEDAGOGICAL' | 'ADMINISTRATIVE' | 'OTHER' | '' = '';
  description = '';
  submitting = false;

  ngOnInit() {
    this.authService.user().pipe(take(1)).subscribe(user => {
      this.currentUser = user;
      this.cdr.detectChanges();
    });
  }

  submit() {
    if (!this.subject || !this.type || !this.description || !this.currentUser?.id) return;
    this.submitting = true;

    const nameParts = (this.currentUser.name || '').split(' ');
    const student = {
      id: Number(this.currentUser.id),
      firstName: nameParts[0] || '',
      lastName: nameParts.slice(1).join(' ') || '',
      email: this.currentUser.email || '',
    };

    this.claimService
      .createClaim({ subject: this.subject, type: this.type as any, description: this.description, status: 'OPEN', student })
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
