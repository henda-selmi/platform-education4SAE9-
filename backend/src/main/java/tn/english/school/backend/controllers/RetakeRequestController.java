package tn.english.school.backend.controllers;

import java.util.HashMap;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.english.school.backend.entities.RetakeRequest;
import tn.english.school.backend.exceptions.ClaimAlreadyLinkedException;
import tn.english.school.backend.services.FileStorageService;
import tn.english.school.backend.services.RetakeRequestService;
import tools.jackson.databind.ObjectMapper;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/retake-requests")
public class RetakeRequestController {

  @Autowired
  private RetakeRequestService retakeRequestService;

  @Autowired
  private FileStorageService fileStorageService;

  @PostMapping(value = "/from-claim/{claimId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<?> createFromClaim(
          @PathVariable Long claimId,
          @RequestPart("request") String requestJson,
          @RequestPart(value = "file", required = false) MultipartFile file) {
    try {
      var mapper = new ObjectMapper();
      RetakeRequest requestDetails = mapper.readValue(requestJson, RetakeRequest.class);

      if (file != null && !file.isEmpty()) {
        String fileName = fileStorageService.storeFile(file);
        requestDetails.setAttachmentPath(fileName);
      }

      var createdRequest = retakeRequestService.createRetakeFromClaim(claimId, requestDetails);
      return ResponseEntity.status(HttpStatus.CREATED).body(createdRequest);

    } catch (Exception e) {
      var errorResponse = new HashMap<String, String>();
      errorResponse.put("error", "Processing Error");
      errorResponse.put("message", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
  }

  @GetMapping
  public ResponseEntity<List<RetakeRequest>> getAll() {
    return ResponseEntity.ok(retakeRequestService.getAllRequests());
  }

  @GetMapping("/{id}")
  public ResponseEntity<RetakeRequest> getById(@PathVariable Long id) {
    return ResponseEntity.ok(retakeRequestService.getRequestById(id));
  }

  @PutMapping("/{id}")
  public ResponseEntity<RetakeRequest> update(@PathVariable Long id,
                                              @RequestBody RetakeRequest request) {
    return ResponseEntity.ok(retakeRequestService.updateRequest(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    retakeRequestService.deleteRequest(id);
    return ResponseEntity.noContent().build();
  }
}