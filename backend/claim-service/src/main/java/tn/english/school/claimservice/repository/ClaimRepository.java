package tn.english.school.claimservice.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.english.school.claimservice.entity.Claim;
import tn.english.school.claimservice.enums.ClaimStatus;

import java.util.List;
import java.util.Optional;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

    boolean existsByStudentIdAndSubjectAndStatusIn(Long studentId, String subject, List<ClaimStatus> statuses);

    @EntityGraph(attributePaths = {"student"})
    Optional<Claim> findById(Long id);

    @EntityGraph(attributePaths = {"student"})
    List<Claim> findAll();
}