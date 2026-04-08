package tn.english.school.claimservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.english.school.claimservice.entity.Claim;
import tn.english.school.claimservice.enums.ClaimStatus;
import tn.english.school.claimservice.enums.ClaimType;
import tn.english.school.claimservice.exception.DuplicateClaimException;
import tn.english.school.claimservice.entity.User;
import tn.english.school.claimservice.repository.ClaimRepository;
import tn.english.school.claimservice.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final UserRepository userRepository;

    public Claim createClaim(Claim claim) {
        // Upsert student so the local users table always reflects current auth-service data
        if (claim.getStudent() != null && claim.getStudent().getId() != null) {
            User incoming = claim.getStudent();
            User student = userRepository.findById(incoming.getId()).orElseGet(User::new);
            student.setId(incoming.getId());
            if (incoming.getFirstName() != null) student.setFirstName(incoming.getFirstName());
            if (incoming.getLastName()  != null) student.setLastName(incoming.getLastName());
            if (incoming.getEmail()     != null) student.setEmail(incoming.getEmail());
            claim.setStudent(userRepository.save(student));
        }
        if (claim.getStudent() != null && claim.getStudent().getId() != null) {
            boolean isDuplicate = claimRepository.existsByStudentIdAndSubjectAndStatusIn(
                    claim.getStudent().getId(),
                    claim.getSubject(),
                    List.of(ClaimStatus.OPEN, ClaimStatus.IN_PROGRESS)
            );
            if (isDuplicate) {
                throw new DuplicateClaimException(
                        "Duplicate Alert: You already have an active claim for this exact subject.");
            }
        }
        claim.setStatus(ClaimStatus.OPEN);
        return claimRepository.save(claim);
    }

    public List<Claim> getAllClaims() {
        return claimRepository.findAll();
    }

    public List<Claim> getClaimsByStudentId(Long studentId) {
        return claimRepository.findByStudentId(studentId);
    }

    public Claim getClaimById(Long id) {
        return claimRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Claim not found with ID: " + id));
    }

    public Claim updateClaim(Long id, Claim updatedClaim) {
        Claim existing = getClaimById(id);

        if (existing.getStatus() != ClaimStatus.OPEN) {
            throw new IllegalStateException(
                    "Action denied: You cannot update a claim that is already being processed or closed.");
        }

        existing.setSubject(updatedClaim.getSubject());
        existing.setDescription(updatedClaim.getDescription());
        existing.setType(updatedClaim.getType());
        return claimRepository.save(existing);
    }

    public void deleteClaim(Long id) {
        Claim existing = getClaimById(id);

        if (existing.getStatus() != ClaimStatus.OPEN) {
            throw new IllegalStateException(
                    "Action denied: You cannot delete a claim that is in progress or resolved.");
        }
        claimRepository.deleteById(id);
    }

    public Claim authorizeRetake(Long id) {
        Claim existing = getClaimById(id);

        if (existing.getType() != ClaimType.PEDAGOGICAL) {
            throw new IllegalStateException(
                    "Action denied: Only pedagogical claims can be authorized for a retake.");
        }
        if (existing.getStatus() == ClaimStatus.RESOLVED || existing.getStatus() == ClaimStatus.CANCELED) {
            throw new IllegalStateException(
                    "Action denied: Cannot authorize a retake for a closed or canceled claim.");
        }
        if (existing.getStatus() == ClaimStatus.RETAKE_AUTHORIZED) {
            return existing;
        }

        existing.setStatus(ClaimStatus.RETAKE_AUTHORIZED);
        return claimRepository.save(existing);
    }

    public Claim linkRetakeRequest(Long claimId, Long retakeRequestId) {
        Claim existing = getClaimById(claimId);
        existing.setRetakeRequestId(retakeRequestId);
        return claimRepository.save(existing);
    }
}