package tn.english.school.claimservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.english.school.claimservice.entity.Claim;
import tn.english.school.claimservice.entity.User;
import tn.english.school.claimservice.enums.ClaimStatus;
import tn.english.school.claimservice.enums.ClaimType;
import tn.english.school.claimservice.exception.DuplicateClaimException;
import tn.english.school.claimservice.repository.ClaimRepository;
import tn.english.school.claimservice.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ClaimService claimService;

    private User student;
    private Claim claim;

    @BeforeEach
    void setUp() {
        student = new User();
        student.setId(1L);
        student.setFirstName("Alice");
        student.setLastName("Martin");
        student.setEmail("alice@esprit.tn");

        claim = new Claim();
        claim.setId(1L);
        claim.setSubject("Exam contestation");
        claim.setDescription("Wrong grade");
        claim.setType(ClaimType.PEDAGOGICAL);
        claim.setStatus(ClaimStatus.OPEN);
        claim.setStudent(student);
    }

    // ── createClaim ──────────────────────────────────────────────────────────

    @Test
    void createClaim_success() {
        claim.setStatus(null); // verify the service sets OPEN
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(userRepository.save(any(User.class))).thenReturn(student);
        when(claimRepository.existsByStudentIdAndSubjectAndStatusIn(1L, "Exam contestation",
                List.of(ClaimStatus.OPEN, ClaimStatus.IN_PROGRESS))).thenReturn(false);
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);

        Claim result = claimService.createClaim(claim);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(ClaimStatus.OPEN);
        verify(claimRepository).save(claim);
    }

    // ── authorizeRetake ───────────────────────────────────────────────────────

    @Test
    void authorizeRetake_success() {
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenAnswer(inv -> inv.getArgument(0));

        Claim result = claimService.authorizeRetake(1L);

        assertThat(result.getStatus()).isEqualTo(ClaimStatus.RETAKE_AUTHORIZED);
        verify(claimRepository).save(claim);
    }

    @Test
    void authorizeRetake_alreadyAuthorized_returnsExistingWithoutSave() {
        claim.setStatus(ClaimStatus.RETAKE_AUTHORIZED);
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));

        Claim result = claimService.authorizeRetake(1L);

        assertThat(result.getStatus()).isEqualTo(ClaimStatus.RETAKE_AUTHORIZED);
        verify(claimRepository, never()).save(any());
    }

    // ── createClaim — doublon ────────────────────────────────────────────────

    @Test
    void createClaim_duplicateActive_throwsDuplicateClaimException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(userRepository.save(any(User.class))).thenReturn(student);
        when(claimRepository.existsByStudentIdAndSubjectAndStatusIn(1L, "Exam contestation",
                List.of(ClaimStatus.OPEN, ClaimStatus.IN_PROGRESS))).thenReturn(true);

        assertThatThrownBy(() -> claimService.createClaim(claim))
                .isInstanceOf(DuplicateClaimException.class)
                .hasMessageContaining("Duplicate Alert");

        verify(claimRepository, never()).save(any());
    }

    // ── updateClaim ───────────────────────────────────────────────────────────

    @Test
    void updateClaim_statusNotOpen_throwsIllegalStateException() {
        claim.setStatus(ClaimStatus.IN_PROGRESS);
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));

        Claim updated = new Claim();
        updated.setSubject("New subject");
        updated.setDescription("New desc");
        updated.setType(ClaimType.TECHNICAL);

        assertThatThrownBy(() -> claimService.updateClaim(1L, updated))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Action denied");

        verify(claimRepository, never()).save(any());
    }

    // ── deleteClaim ───────────────────────────────────────────────────────────

    @Test
    void deleteClaim_statusNotOpen_throwsIllegalStateException() {
        claim.setStatus(ClaimStatus.RESOLVED);
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));

        assertThatThrownBy(() -> claimService.deleteClaim(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Action denied");

        verify(claimRepository, never()).deleteById(any());
    }

    // ── authorizeRetake — cas d'erreur ────────────────────────────────────────

    @Test
    void authorizeRetake_notPedagogical_throwsIllegalStateException() {
        claim.setType(ClaimType.TECHNICAL);
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));

        assertThatThrownBy(() -> claimService.authorizeRetake(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only pedagogical claims");

        verify(claimRepository, never()).save(any());
    }

    @Test
    void authorizeRetake_alreadyResolved_throwsIllegalStateException() {
        claim.setStatus(ClaimStatus.RESOLVED);
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));

        assertThatThrownBy(() -> claimService.authorizeRetake(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("closed or canceled");

        verify(claimRepository, never()).save(any());
    }

    @Test
    void authorizeRetake_alreadyCanceled_throwsIllegalStateException() {
        claim.setStatus(ClaimStatus.CANCELED);
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));

        assertThatThrownBy(() -> claimService.authorizeRetake(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("closed or canceled");

        verify(claimRepository, never()).save(any());
    }
}
