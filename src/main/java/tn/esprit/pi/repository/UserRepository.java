package tn.esprit.pi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.domain.AppUser;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
}
