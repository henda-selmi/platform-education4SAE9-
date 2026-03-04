package tn.english.school.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.english.school.backend.entities.RetakeRequest;

public interface RetakeRequestRepository extends JpaRepository<RetakeRequest, Long> {

}