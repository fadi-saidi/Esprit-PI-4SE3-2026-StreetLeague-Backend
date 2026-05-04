package tn.esprit.pi.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.domain.Post;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUserId(Long userId);
    List<Post> findByFlaggedTrue();
}
