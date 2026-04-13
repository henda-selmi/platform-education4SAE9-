package tn.english.school.retakeservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.english.school.retakeservice.entity.RetakeRequest;
import tn.english.school.retakeservice.enums.RequestStatus;
import tn.english.school.retakeservice.service.RetakeRequestService;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/retake-requests")
@RequiredArgsConstructor
public class RetakeRequestController {

    private final RetakeRequestService retakeRequestService;

    @Value("${file.upload-dir}")
    private String uploadDir;

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

    @PatchMapping("/{id}/approve")
    public ResponseEntity<RetakeRequest> approveRequest(@PathVariable Long id) {
        return ResponseEntity.ok(retakeRequestService.approveOrDenyRequest(id, RequestStatus.APPROVED));
    }

    @PatchMapping("/{id}/deny")
    public ResponseEntity<RetakeRequest> denyRequest(@PathVariable Long id) {
        return ResponseEntity.ok(retakeRequestService.approveOrDenyRequest(id, RequestStatus.DENIED));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequest(@PathVariable Long id) {
        retakeRequestService.deleteRequest(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/validate-document")
    public ResponseEntity<RetakeRequest> validateDocument(@PathVariable Long id) {
        return ResponseEntity.ok(retakeRequestService.validateDocument(id));
    }

    @PatchMapping("/{id}/reject-document")
    public ResponseEntity<RetakeRequest> rejectDocument(@PathVariable Long id,
                                                         @RequestBody java.util.Map<String, String> body) {
        return ResponseEntity.ok(retakeRequestService.rejectDocument(id, body.get("reason")));
    }

    @PatchMapping(value = "/{id}/resubmit-document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RetakeRequest> resubmitDocument(@PathVariable Long id,
                                                           @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(retakeRequestService.resubmitDocument(id, file));
    }

    @GetMapping("/attachments/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
