package tn.english.school.retakeservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import tn.english.school.retakeservice.dto.ClaimDTO;

@FeignClient(name = "claim-service")
public interface ClaimClient {

    @GetMapping("/api/claims/{id}")
    ClaimDTO getClaimById(@PathVariable("id") Long id);

    @PutMapping("/api/claims/{claimId}/link-retake/{retakeRequestId}")
    ClaimDTO linkRetakeRequest(
            @PathVariable("claimId") Long claimId,
            @PathVariable("retakeRequestId") Long retakeRequestId
    );
}