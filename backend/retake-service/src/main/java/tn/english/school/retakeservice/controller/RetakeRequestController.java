package tn.english.school.retakeservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.english.school.retakeservice.entity.RetakeRequest;
import tn.english.school.retakeservice.service.RetakeRequestService;

import java.util.List;

@RestController
@RequestMapping("/api/retake-requests")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class RetakeRequestController {

    private final RetakeRequestService retakeRequestService;

    @PostMapping(value = "/from-claim/{claimId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RetakeRequest> createRetakeFromClaim(
            @PathVariable Long claimId,
            @RequestPart("retakeRequest") RetakeRequest retakeRequest,
            @RequestPart(value = "attachment", required = false) MultipartFile attachment) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(retakeRequestService.createRetakeFromClaim(claimId, retakeRequest, attachment));
    }

    @GetMapping
    public ResponseEntity<List<RetakeRequest>> getAllRequests() {
        return ResponseEntity.ok(retakeRequestService.getAllRequests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RetakeRequest> getRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(retakeRequestService.getRequestById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RetakeRequest> updateRequest(
            @PathVariable Long id,
            @RequestBody RetakeRequest retakeRequest) {
        return ResponseEntity.ok(retakeRequestService.updateRequest(id, retakeRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequest(@PathVariable Long id) {
        retakeRequestService.deleteRequest(id);
        return ResponseEntity.noContent().build();
    }
}
