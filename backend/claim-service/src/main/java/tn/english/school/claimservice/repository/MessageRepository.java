package tn.english.school.claimservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.english.school.claimservice.entity.Message;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByClaimIdOrderByCreatedAtAsc(Long claimId);
}
