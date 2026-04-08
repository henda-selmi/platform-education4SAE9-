package tn.english.school.claimservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.english.school.claimservice.entity.Claim;
import tn.english.school.claimservice.enums.ClaimType;
import tn.english.school.claimservice.service.ClaimService;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;

    @PostMapping
    public ResponseEntity<Claim> createClaim(@RequestBody Claim claim) {
        return ResponseEntity.status(HttpStatus.CREATED).body(claimService.createClaim(claim));
    }

    @GetMapping
    public ResponseEntity<List<Claim>> getAllClaims(@RequestParam(required = false) Long studentId) {
        if (studentId != null) {
            return ResponseEntity.ok(claimService.getClaimsByStudentId(studentId));
        }
        return ResponseEntity.ok(claimService.getAllClaims());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Claim> getClaimById(@PathVariable Long id) {
        return ResponseEntity.ok(claimService.getClaimById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Claim> updateClaim(@PathVariable Long id, @RequestBody Claim claim) {
        return ResponseEntity.ok(claimService.updateClaim(id, claim));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClaim(@PathVariable Long id) {
        claimService.deleteClaim(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/types")
    public ResponseEntity<ClaimType[]> getClaimTypes() {
        return ResponseEntity.ok(ClaimType.values());
    }

    @PatchMapping("/{id}/authorize-retake")
    public ResponseEntity<Claim> authorizeRetake(@PathVariable Long id) {
        return ResponseEntity.ok(claimService.authorizeRetake(id));
    }

    // Called internally by retake-service to link a retake request back to this claim
    @PutMapping("/{claimId}/link-retake/{retakeRequestId}")
    public ResponseEntity<Claim> linkRetakeRequest(
            @PathVariable Long claimId,
            @PathVariable Long retakeRequestId) {
        return ResponseEntity.ok(claimService.linkRetakeRequest(claimId, retakeRequestId));
    }
}