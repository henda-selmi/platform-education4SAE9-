package tn.english.school.backend.services;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.english.school.backend.entities.Claim;
import tn.english.school.backend.enums.ClaimStatus;
import tn.english.school.backend.enums.ClaimType;
import tn.english.school.backend.exceptions.DuplicateClaimException;
import tn.english.school.backend.repositories.ClaimRepository;

@Service
public class ClaimService {

  @Autowired
  private ClaimRepository claimRepository;

  public Claim createClaim(Claim claim) {
    if (claim.getStudent() != null && claim.getStudent().getId() != null) {
      var isDuplicate = claimRepository.existsByStudentIdAndSubjectAndStatusIn(
              claim.getStudent().getId(),
              claim.getSubject(),
              List.of(ClaimStatus.OPEN, ClaimStatus.IN_PROGRESS)
      );
      if (isDuplicate) {
        throw new DuplicateClaimException(
                "Duplicate Alert: You already have an active claim for this exact subject.");
      }
    }
    // Ensures a new claim is always OPEN, regardless of what the JSON payload said
    claim.setStatus(ClaimStatus.OPEN);
    return claimRepository.save(claim);
  }

  public List<Claim> getAllClaims() {
    return claimRepository.findAll();
  }

  public Claim getClaimById(Long id) {
    return claimRepository.findById(id).orElseThrow(() -> new RuntimeException("Claim not found"));
  }

  public Claim updateClaim(Long id, Claim updatedClaim) {
    var existingClaim = getClaimById(id);

    // RULE: If the admin started working on it (IN_PROGRESS) or finished it, the student cannot update it.
    if (existingClaim.getStatus() != ClaimStatus.OPEN) {
      throw new IllegalStateException("Action denied: You cannot update a claim that is already being processed or closed.");
    }

    existingClaim.setSubject(updatedClaim.getSubject());
    existingClaim.setDescription(updatedClaim.getDescription());
    existingClaim.setType(updatedClaim.getType());

    // Notice I removed `existingClaim.setStatus()`. Students shouldn't be able to change status via PUT.
    return claimRepository.save(existingClaim);
  }

  public void deleteClaim(Long id) {
    var existingClaim = getClaimById(id);

    // RULE: Cannot delete a claim unless it's still just OPEN.
    if (existingClaim.getStatus() != ClaimStatus.OPEN) {
      throw new IllegalStateException("Action denied: You cannot delete a claim that is in progress or resolved.");
    }

    claimRepository.deleteById(id);
  }

  // ==========================================
  // NOUVELLE MÉTHODE : WORKFLOW DE RATTRAPAGE
  // ==========================================

  public Claim authorizeRetake(Long id) {
    var existingClaim = getClaimById(id);

    // RÈGLE 1 : Seules les réclamations pédagogiques peuvent donner droit à un rattrapage.
    if (existingClaim.getType() != ClaimType.PEDAGOGICAL) {
      throw new IllegalStateException("Action denied: Only pedagogical claims can be authorized for a retake.");
    }

    // RÈGLE 2 : On ne peut pas autoriser un rattrapage sur un ticket déjà résolu ou annulé.
    if (existingClaim.getStatus() == ClaimStatus.RESOLVED || existingClaim.getStatus() == ClaimStatus.CANCELED) {
      throw new IllegalStateException("Action denied: Cannot authorize a retake for a closed or canceled claim.");
    }

    // RÈGLE 3 : Si c'est déjà autorisé, on ne fait rien (idempotence).
    if (existingClaim.getStatus() == ClaimStatus.RETAKE_AUTHORIZED) {
      return existingClaim;
    }

    // Application du nouveau statut
    existingClaim.setStatus(ClaimStatus.RETAKE_AUTHORIZED);

    return claimRepository.save(existingClaim);
  }
}