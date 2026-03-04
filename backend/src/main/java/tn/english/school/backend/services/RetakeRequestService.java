package tn.english.school.backend.services;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.english.school.backend.entities.RetakeRequest;
import tn.english.school.backend.enums.ClaimStatus;
import tn.english.school.backend.enums.ClaimType;
import tn.english.school.backend.exceptions.ClaimAlreadyLinkedException;
import tn.english.school.backend.repositories.ClaimRepository;
import tn.english.school.backend.repositories.RetakeRequestRepository;

@Service
public class RetakeRequestService {

  @Autowired
  private RetakeRequestRepository retakeRequestRepository;

  @Autowired
  private ClaimRepository claimRepository;

  public List<RetakeRequest> getAllRequests() {
    return retakeRequestRepository.findAll();
  }

  public RetakeRequest getRequestById(Long id) {
    return retakeRequestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Request not found"));
  }

  public RetakeRequest updateRequest(Long id, RetakeRequest updatedRequest) {
    var existingRequest = getRequestById(id);

    if (existingRequest.getClaim().getStatus() != ClaimStatus.OPEN) {
      throw new IllegalStateException("Action denied: Cannot update retake request details while the claim is being processed.");
    }

    existingRequest.setCourseName(updatedRequest.getCourseName());
    existingRequest.setReason(updatedRequest.getReason());
    return retakeRequestRepository.save(existingRequest);
  }

  public void deleteRequest(Long id) {
    var existingRequest = getRequestById(id);

    if (existingRequest.getClaim().getStatus() != ClaimStatus.OPEN) {
      throw new IllegalStateException("Action denied: Cannot delete a retake request that is being processed.");
    }
    retakeRequestRepository.deleteById(id);
  }

  public RetakeRequest createRetakeFromClaim(Long claimId, RetakeRequest retakeRequestDetails) {
    var claim = claimRepository.findById(claimId)
            .orElseThrow(() -> new RuntimeException("Claim not found with ID: " + claimId));

    if (claim.getType() != ClaimType.PEDAGOGICAL) {
      throw new IllegalStateException("Rule Violation: Retake requests can only be linked to claims of type 'PEDAGOGICAL'.");
    }

    if (claim.getRetakeRequest() != null) {
      throw new ClaimAlreadyLinkedException(
              "Rule Violation: A retake request is already linked to this claim.");
    }

    if (claim.getStatus() != ClaimStatus.RETAKE_AUTHORIZED) {
      throw new IllegalStateException("Action denied: You cannot submit a retake request until the administration authorizes it.");
    }

    retakeRequestDetails.setClaim(claim);
    retakeRequestDetails.setStudent(claim.getStudent());

    return retakeRequestRepository.save(retakeRequestDetails);
  }}