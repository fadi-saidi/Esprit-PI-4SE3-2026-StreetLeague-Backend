package tn.esprit.pi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.domain.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
