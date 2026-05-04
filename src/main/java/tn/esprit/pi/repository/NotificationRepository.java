package tn.esprit.pi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.domain.Notification;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findByUserIdAndReadFalse(Long userId);
    long countByUserIdAndReadFalse(Long userId);
}
