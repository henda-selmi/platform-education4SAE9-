package tn.english.school.claimservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.english.school.claimservice.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
