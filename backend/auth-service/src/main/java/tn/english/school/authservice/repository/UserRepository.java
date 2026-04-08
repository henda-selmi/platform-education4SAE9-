package tn.english.school.authservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.english.school.authservice.entity.AppUser;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
    boolean existsByEmail(String email);
}
