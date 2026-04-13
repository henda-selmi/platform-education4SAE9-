package tn.english.school.authservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.english.school.authservice.entity.StudentAcademicProfile;

import java.util.Optional;

public interface StudentAcademicProfileRepository extends JpaRepository<StudentAcademicProfile, Long> {
    Optional<StudentAcademicProfile> findByStudentId(Long studentId);
    boolean existsByStudentId(Long studentId);
}