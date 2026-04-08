package tn.english.school.retakeservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.english.school.retakeservice.entity.RetakeRequest;

import java.util.Optional;

public interface RetakeRequestRepository extends JpaRepository<RetakeRequest, Long> {

    Optional<RetakeRequest> findByClaimId(Long claimId);

    boolean existsByClaimId(Long claimId);
}
