package tn.esprit.pi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.pi.domain.MerchStatus;
import tn.esprit.pi.domain.PlayerMerch;
import tn.esprit.pi.domain.PlayerProfile;

import java.util.List;

public interface PlayerMerchRepository extends JpaRepository<PlayerMerch, Long> {
    
    List<PlayerMerch> findBySellerOrderBySubmittedAtDesc(PlayerProfile seller);
    
    Page<PlayerMerch> findByStatusOrderBySubmittedAtDesc(MerchStatus status, Pageable pageable);
    
    @Query("SELECT COUNT(pm) FROM PlayerMerch pm WHERE pm.status = :status")
    Long countByStatus(@Param("status") MerchStatus status);
}