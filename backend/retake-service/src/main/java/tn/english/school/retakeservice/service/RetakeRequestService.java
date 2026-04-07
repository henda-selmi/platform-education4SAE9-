package tn.english.school.retakeservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.english.school.retakeservice.client.ClaimClient;
import tn.english.school.retakeservice.dto.ClaimDTO;
import tn.english.school.retakeservice.entity.RetakeRequest;
import tn.english.school.retakeservice.exception.ClaimAlreadyLinkedException;
import tn.english.school.retakeservice.exception.InvalidRetakeException;
import tn.english.school.retakeservice.repository.RetakeRequestRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RetakeRequestService {

    private final RetakeRequestRepository retakeRequestRepository;
    private final ClaimClient claimClient;
    private final FileStorageService fileStorageService;

    public List<RetakeRequest> getAllRequests() {
        return retakeRequestRepository.findAll();
    }

    public RetakeRequest getRequestById(Long id) {
        return retakeRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Retake request not found with ID: " + id));
    }

    public RetakeRequest updateRequest(Long id, RetakeRequest updatedRequest) {
        RetakeRequest existing = getRequestById(id);

        // Fetch the linked claim via Feign to enforce the same business rule
        ClaimDTO claim = claimClient.getClaimById(existing.getClaimId());
        if (!"OPEN".equals(claim.getStatus())) {
            throw new IllegalStateException(
                    "Action denied: Cannot update retake request details while the claim is being processed.");
        }

        existing.setCourseName(updatedRequest.getCourseName());
        existing.setReason(updatedRequest.getReason());
        return retakeRequestRepository.save(existing);
    }

    public void deleteRequest(Long id) {
        RetakeRequest existing = getRequestById(id);

        ClaimDTO claim = claimClient.getClaimById(existing.getClaimId());
        if (!"OPEN".equals(claim.getStatus())) {
            throw new IllegalStateException(
                    "Action denied: Cannot delete a retake request that is being processed.");
        }
        retakeRequestRepository.deleteById(id);
    }

    public RetakeRequest createRetakeFromClaim(Long claimId, RetakeRequest retakeDetails, MultipartFile attachment) {
        // Fetch the claim from claim-service via Feign
        ClaimDTO claim = claimClient.getClaimById(claimId);

        if (!"PEDAGOGICAL".equals(claim.getType())) {
            throw new InvalidRetakeException(
                    "Rule Violation: Retake requests can only be linked to claims of type 'PEDAGOGICAL'.");
        }
        if (retakeRequestRepository.existsByClaimId(claimId)) {
            throw new ClaimAlreadyLinkedException(
                    "Rule Violation: A retake request is already linked to this claim.");
        }
        if (!"RETAKE_AUTHORIZED".equals(claim.getStatus())) {
            throw new IllegalStateException(
                    "Action denied: You cannot submit a retake request until the administration authorizes it.");
        }

        retakeDetails.setClaimId(claimId);
        if (claim.getStudent() != null) {
            retakeDetails.setStudentId(claim.getStudent().getId());
        }

        if (attachment != null && !attachment.isEmpty()) {
            retakeDetails.setAttachmentPath(fileStorageService.storeFile(attachment));
        }

        RetakeRequest saved = retakeRequestRepository.save(retakeDetails);

        // Notify claim-service to record the link
        claimClient.linkRetakeRequest(claimId, saved.getId());

        return saved;
    }
}
