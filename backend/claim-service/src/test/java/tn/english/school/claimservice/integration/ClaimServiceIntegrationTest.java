package tn.english.school.claimservice.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import tn.english.school.claimservice.entity.Claim;
import tn.english.school.claimservice.entity.User;
import tn.english.school.claimservice.enums.ClaimStatus;
import tn.english.school.claimservice.enums.ClaimType;
import tn.english.school.claimservice.exception.DuplicateClaimException;
import tn.english.school.claimservice.repository.ClaimRepository;
import tn.english.school.claimservice.repository.UserRepository;
import tn.english.school.claimservice.service.ClaimService;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ClaimServiceIntegrationTest {

    @Autowired
    private ClaimService claimService;

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private UserRepository userRepository;

    private User student;

    @BeforeEach
    void setUp() {
        student = new User();
        student.setId(99L);
        student.setFirstName("Bob");
        student.setLastName("Dupont");
        student.setEmail("bob@esprit.tn");
        userRepository.save(student);
    }

    // ── Persistance basique ───────────────────────────────────────────────────

    @Test
    void createClaim_persistedInDb_withGeneratedId() {
        Claim claim = buildClaim("Math exam grade", ClaimType.PEDAGOGICAL);

        Claim saved = claimService.createClaim(claim);

        // Re-fetch from DB to verify real persistence (not just session cache)
        Claim fromDb = claimRepository.findById(saved.getId())
                .orElseThrow(() -> new AssertionError("Claim not found in DB"));
        assertThat(fromDb.getId()).isNotNull();
        assertThat(fromDb.getStatus()).isEqualTo(ClaimStatus.OPEN);
    }

    // ── Anti-doublon persisté ─────────────────────────────────────────────────

    @Test
    void createClaim_duplicate_throwsDuplicateClaimException() {
        Claim first = buildClaim("Physics exam", ClaimType.PEDAGOGICAL);
        claimService.createClaim(first);
        // Flush to DB so the duplicate check hits the real table, not the session cache
        claimRepository.flush();

        Claim duplicate = buildClaim("Physics exam", ClaimType.PEDAGOGICAL);

        assertThatThrownBy(() -> claimService.createClaim(duplicate))
                .isInstanceOf(DuplicateClaimException.class)
                .hasMessageContaining("Duplicate Alert");
    }

    // ── Cycle complet ─────────────────────────────────────────────────────────

    @Test
    void fullCycle_create_authorize_link() {
        // 1. Créer un claim pédagogique
        Claim claim = buildClaim("Biology retake", ClaimType.PEDAGOGICAL);
        Claim created = claimService.createClaim(claim);
        assertThat(created.getStatus()).isEqualTo(ClaimStatus.OPEN);

        // 2. Autoriser le retake
        Claim authorized = claimService.authorizeRetake(created.getId());
        assertThat(authorized.getStatus()).isEqualTo(ClaimStatus.RETAKE_AUTHORIZED);

        // 3. Lier un retake request
        Claim linked = claimService.linkRetakeRequest(authorized.getId(), 42L);
        assertThat(linked.getRetakeRequestId()).isEqualTo(42L);
        assertThat(linked.getStatus()).isEqualTo(ClaimStatus.RETAKE_AUTHORIZED);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Claim buildClaim(String subject, ClaimType type) {
        Claim claim = new Claim();
        claim.setSubject(subject);
        claim.setDescription("Test description");
        claim.setType(type);
        claim.setStudent(student);
        return claim;
    }
}