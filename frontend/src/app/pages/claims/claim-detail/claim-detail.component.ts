import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ClaimService } from '../../../core/services/claim.service';
import { Claim } from '../../../core/models/claim.model';
import { RetakeRequest } from '../../../core/models/retake-request.model';
import { NotificationService } from 'carbon-components-angular';

@Component({
  selector: 'app-claim-detail',
  templateUrl: './claim-detail.component.html'
})
export class ClaimDetailComponent implements OnInit {
  public claim: Claim | null = null;
  public loading = true;
  
  // 1. Variable pour stocker le fichier sélectionné
  public selectedFile: File | null = null;

  public retakeDetails: RetakeRequest = {
    courseName: '',
    reason: '',
    status: 'PENDING'
  };

  constructor(
    private route: ActivatedRoute,
    private claimService: ClaimService,
    private router: Router,
    private notificationService: NotificationService
  ) { }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadClaim(Number(id));
    } else {
      this.goBack();
    }
  }

  loadClaim(id: number): void {
    this.claimService.getClaimById(id).subscribe({
      next: (data: Claim) => {
        this.claim = data;
        this.loading = false;
      },
      error: () => this.goBack()
    });
  }

  // 2. Méthode pour capturer le fichier depuis l'input HTML
  onFileSelected(event: any): void {
    if (event.target.files && event.target.files.length > 0) {
      this.selectedFile = event.target.files[0];
    }
  }

  // 3. Mise à jour de la soumission pour inclure le fichier
  submitRetake(): void {
    if (!this.claim?.id) return;

    if (!this.retakeDetails.courseName || !this.retakeDetails.reason) {
      this.showNotification('warning', 'Incomplete', 'Please provide Exam Name, Reason and a document.');
      return;
    }

    if (!this.selectedFile) {
      this.showNotification('warning', 'Missing File', 'Please upload a justification document (PDF or Image).');
      return;
    }

    // On passe maintenant claim.id, retakeDetails ET le fichier
    this.claimService.createRetakeFromClaim(this.claim.id, this.retakeDetails, this.selectedFile).subscribe({
      next: () => {
        this.showNotification('success', 'Submitted', 'Retake request with document submitted successfully.');
        this.selectedFile = null; // Reset le fichier
        this.loadClaim(this.claim!.id!); 
      },
      error: (err) => {
        // Accès au message d'erreur si le backend renvoie un JSON
        const errorMessage = err.error?.message || 'Failed to submit retake request.';
        this.showNotification('error', 'Error', errorMessage);
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/app/claims']);
  }

  showNotification(type: any, title: string, message: string): void {
    this.notificationService.showNotification({
      type, title, message, target: '.notification-container', smart: true
    });
  }
}