package tn.esprit.pi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.domain.Product;
import tn.esprit.pi.domain.ProductReview;
import tn.esprit.pi.domain.User;

import java.util.List;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
    List<ProductReview> findByProductOrderByCreatedAtDesc(Product product);
    boolean existsByProductAndUser(Product product, User user);
}
