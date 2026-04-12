import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule, NgStyle } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { PageHeader } from '@shared';
import { AuthService } from '@core/authentication';
import { ClaimService } from '../services/claim.service';
import { Claim, Message } from '../models/claim.model';

@Component({
  selector: 'app-claim-detail',
  templateUrl: './claim-detail.component.html',
  styleUrl: './claim-detail.component.scss',
  imports: [
    CommonModule,
    NgStyle,
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatDividerModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatTooltipModule,
    PageHeader,
  ],
})
export class ClaimDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly claimService = inject(ClaimService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly sanitizer = inject(DomSanitizer);
  private readonly authService = inject(AuthService);

  claim: Claim | null = null;
  isAdmin = false;
  loading = true;
  selectedFile: File | null = null;
  courseName = '';
  reason = '';
  submitting = false;
  authorizing = false;

  // Document verification
  showRejectInput = false;
  rejectReason = '';
  resubmitFile: File | null = null;
  verifying = false;

  // Messaging
  messages: Message[] = [];
  newMessage = '';
  sendingMessage = false;
  drafting = false;
  currentUserId: number | null = null;
  currentUserName = '';
  currentUserRole: 'ADMIN' | 'STUDENT' = 'STUDENT';

  readonly typeLabels: Record<string, string> = {
    TECHNICAL: 'Technical',
    PEDAGOGICAL: 'Pedagogical',
    ADMINISTRATIVE: 'Administrative',
    OTHER: 'Other',
  };

  readonly statusLabels: Record<string, string> = {
    OPEN: 'Open',
    IN_PROGRESS: 'In Progress',
    RETAKE_AUTHORIZED: 'Retake Authorized',
    RESOLVED: 'Resolved',
    REJECTED: 'Rejected',
    CANCELED: 'Canceled',
  };

  ngOnInit() {
    this.authService.user().subscribe(user => {
      this.isAdmin = user.roles?.includes('ADMIN') ?? false;
      this.currentUserId = user.id ? Number(user.id) : null;
      this.currentUserName = user.name || '';
      this.currentUserRole = this.isAdmin ? 'ADMIN' : 'STUDENT';
      this.cdr.markForCheck();
    });

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadClaim(Number(id));
    } else {
      this.goBack();
    }
  }

  loadClaim(id: number) {
    this.loading = true;
    this.claimService.getClaimById(id).subscribe({
      next: data => {
        this.claim = data;
        this.loadMessages(id);
        if (data.retakeRequestId) {
          this.claimService.getRetakeRequestById(data.retakeRequestId).subscribe({
            next: retake => {
              this.claim!.retakeRequest = retake;
              this.loading = false;
              this.cdr.markForCheck();
            },
            error: () => {
              this.loading = false;
              this.cdr.markForCheck();
            },
          });
        } else {
          this.loading = false;
          this.cdr.markForCheck();
        }
      },
      error: () => this.goBack(),
    });
  }

  loadMessages(claimId: number) {
    this.claimService.getMessages(claimId).subscribe({
      next: msgs => {
        this.messages = msgs;
        this.cdr.markForCheck();
      },
    });
  }

  draftResponse() {
    if (!this.claim?.id) return;
    this.drafting = true;
    this.claimService.draftResponse(this.claim.id).subscribe({
      next: res => {
        this.newMessage = res.draft;
        this.drafting = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.drafting = false;
        this.snackBar.open('AI draft unavailable, please write your response manually.', 'Close', { duration: 3000 });
        this.cdr.markForCheck();
      },
    });
  }

  sendMessage() {
    if (!this.newMessage.trim() || !this.claim?.id || !this.currentUserId) return;
    this.sendingMessage = true;
    const message: Partial<Message> = {
      senderId: this.currentUserId,
      senderName: this.currentUserName,
      senderRole: this.currentUserRole,
      content: this.newMessage.trim(),
    };
    this.claimService.sendMessage(this.claim.id, message).subscribe({
      next: msg => {
        this.messages = [...this.messages, msg];
        this.newMessage = '';
        this.sendingMessage = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.sendingMessage = false;
        this.snackBar.open('Failed to send message.', 'Close', { duration: 3000 });
        this.cdr.markForCheck();
      },
    });
  }

  getStudentFullName(): string {
    const s = this.claim?.student;
    if (!s) return 'Unknown Student';
    const name = [s.firstName, s.lastName].filter(Boolean).join(' ');
    return name || `Student #${s.id}`;
  }

  getStudentInitials(): string {
    const s = this.claim?.student;
    if (!s) return '?';
    if (s.firstName && s.lastName) return (s.firstName[0] + s.lastName[0]).toUpperCase();
    if (s.firstName) return s.firstName[0].toUpperCase();
    if (s.lastName) return s.lastName[0].toUpperCase();
    return '#';
  }

  getAttachmentUrl(attachmentPath: string): string {
    return this.claimService.getAttachmentUrl(attachmentPath);
  }

  safeAttachmentUrl(attachmentPath: string): SafeResourceUrl {
    return this.sanitizer.bypassSecurityTrustResourceUrl(
      this.claimService.getAttachmentUrl(attachmentPath)
    );
  }

  getAttachmentFilename(attachmentPath: string): string {
    return attachmentPath.split(/[/\\]/).pop() || attachmentPath;
  }

  downloadAttachment(attachmentPath: string) {
    this.claimService.downloadAttachment(attachmentPath);
  }

  isImageAttachment(attachmentPath: string): boolean {
    const lower = attachmentPath.toLowerCase();
    return lower.endsWith('.png') || lower.endsWith('.jpg') || lower.endsWith('.jpeg');
  }

  isPdfAttachment(attachmentPath: string): boolean {
    return attachmentPath.toLowerCase().endsWith('.pdf');
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
    }
  }

  submitRetake() {
    if (!this.claim?.id) return;
    if (!this.courseName || !this.reason) {
      this.snackBar.open('Please provide exam name and reason.', 'Close', { duration: 3000 });
      return;
    }
    if (!this.selectedFile) {
      this.snackBar.open('Please upload a justification document.', 'Close', { duration: 3000 });
      return;
    }
    this.submitting = true;
    this.claimService
      .createRetakeFromClaim(this.claim.id, this.courseName, this.reason, this.selectedFile)
      .subscribe({
        next: () => {
          this.snackBar.open('Retake request submitted successfully.', 'Close', { duration: 4000 });
          this.submitting = false;
          this.selectedFile = null;
          this.courseName = '';
          this.reason = '';
          this.cdr.markForCheck();
          this.loadClaim(this.claim!.id!);
        },
        error: err => {
          const msg = err.error?.message || 'Failed to submit retake request.';
          this.snackBar.open(msg, 'Close', { duration: 4000 });
          this.submitting = false;
          this.cdr.markForCheck();
        },
      });
  }

  authorizeRetake() {
    if (!this.claim?.id) return;
    this.authorizing = true;
    this.claimService.authorizeRetake(this.claim.id).subscribe({
      next: () => {
        this.snackBar.open('Retake authorized successfully.', 'Close', { duration: 3000 });
        this.authorizing = false;
        this.cdr.markForCheck();
        this.loadClaim(this.claim!.id!);
      },
      error: () => {
        this.snackBar.open('Failed to authorize retake.', 'Close', { duration: 3000 });
        this.authorizing = false;
        this.cdr.markForCheck();
      },
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

  retakeStatusStyle(status: string): Record<string, string> {
    const map: Record<string, Record<string, string>> = {
      PENDING:  { background: 'rgba(251,191,36,0.18)', color: '#d97706', border: '1.5px solid rgba(251,191,36,0.6)' },
      APPROVED: { background: 'rgba(16,185,129,0.18)', color: '#059669', border: '1.5px solid rgba(16,185,129,0.6)' },
      DENIED:   { background: 'rgba(239,68,68,0.18)',  color: '#dc2626', border: '1.5px solid rgba(239,68,68,0.6)' },
    };
    return map[status] ?? { background: '#f3f4f6', color: '#374151', border: '1px solid #d1d5db' };
  }

  validateDocument() {
    if (!this.claim?.retakeRequest?.id) return;
    this.verifying = true;
    this.claimService.validateDocument(this.claim.retakeRequest.id).subscribe({
      next: updated => {
        this.claim!.retakeRequest = updated;
        this.verifying = false;
        this.snackBar.open('Document marked as valid.', 'Close', { duration: 3000 });
        this.cdr.markForCheck();
      },
      error: () => { this.verifying = false; this.cdr.markForCheck(); },
    });
  }

  rejectDocument() {
    if (!this.claim?.retakeRequest?.id || !this.rejectReason.trim()) return;
    this.verifying = true;
    this.claimService.rejectDocument(this.claim.retakeRequest.id, this.rejectReason.trim()).subscribe({
      next: updated => {
        this.claim!.retakeRequest = updated;
        this.verifying = false;
        this.showRejectInput = false;
        this.rejectReason = '';
        this.snackBar.open('Document rejected. Student will be notified.', 'Close', { duration: 3000 });
        this.cdr.markForCheck();
      },
      error: () => { this.verifying = false; this.cdr.markForCheck(); },
    });
  }

  onResubmitFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) this.resubmitFile = input.files[0];
  }

  resubmitDocument() {
    if (!this.claim?.retakeRequest?.id || !this.resubmitFile) return;
    this.verifying = true;
    this.claimService.resubmitDocument(this.claim.retakeRequest.id, this.resubmitFile).subscribe({
      next: updated => {
        this.claim!.retakeRequest = updated;
        this.resubmitFile = null;
        this.verifying = false;
        this.snackBar.open('Document resubmitted successfully.', 'Close', { duration: 3000 });
        this.cdr.markForCheck();
      },
      error: () => { this.verifying = false; this.cdr.markForCheck(); },
    });
  }

  goBack() {
    this.router.navigate(['/claims']);
  }
}
