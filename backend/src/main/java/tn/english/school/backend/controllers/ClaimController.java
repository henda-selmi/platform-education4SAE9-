package tn.english.school.backend.controllers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.english.school.backend.entities.Claim;
import tn.english.school.backend.enums.ClaimType;
import tn.english.school.backend.exceptions.DuplicateClaimException;
import tn.english.school.backend.services.ClaimService;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/claims")
public class ClaimController {

  @Autowired
  private ClaimService claimService;

  @PostMapping
  public ResponseEntity<?> create(@RequestBody Claim claim) {
    try {
      var savedClaim = claimService.createClaim(claim);
      return ResponseEntity.status(HttpStatus.CREATED).body(savedClaim);

    } catch (DuplicateClaimException e) {
      var errorResponse = new HashMap<>();
      errorResponse.put("error", "Conflict");
      errorResponse.put("message", e.getMessage());

      return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
  }

  @GetMapping
  public ResponseEntity<List<Claim>> getAll() {
    return ResponseEntity.ok(claimService.getAllClaims());
  }

  @GetMapping("/{id}")
  public ResponseEntity<Claim> getById(@PathVariable Long id) {
    return ResponseEntity.ok(claimService.getClaimById(id));
  }

  @PutMapping("/{id}")
  public ResponseEntity<Claim> update(@PathVariable Long id, @RequestBody Claim claim) {
    return ResponseEntity.ok(claimService.updateClaim(id, claim));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    claimService.deleteClaim(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/types")
  public ResponseEntity<List<String>> getClaimTypes() {
    var types = Arrays.stream(ClaimType.values())
            .map(Enum::name)
            .collect(Collectors.toList());

    return ResponseEntity.ok(types);
  }

  @PatchMapping("/{id}/authorize-retake")
  public ResponseEntity<?> authorizeRetake(@PathVariable Long id) {
    try {
      var updatedClaim = claimService.authorizeRetake(id);
      return ResponseEntity.ok(updatedClaim);
    } catch (IllegalStateException e) {
      // Renvoie une erreur 400 (Bad Request) si les règles métier ne sont pas respectées
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (RuntimeException e) {
      // Renvoie une erreur 404 (Not Found) si le claim n'existe pas
      return ResponseEntity.notFound().build();
    }
  }
}