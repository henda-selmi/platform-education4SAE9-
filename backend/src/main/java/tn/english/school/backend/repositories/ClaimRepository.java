package tn.english.school.backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.english.school.backend.entities.Claim;
import tn.english.school.backend.enums.ClaimStatus;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

  boolean existsByStudentIdAndSubjectAndStatusIn(Long studentId, String subject,
                                                 List<ClaimStatus> statuses);

  @Override
  @EntityGraph(attributePaths = {"student", "retakeRequest"})
  List<Claim> findAll();
}