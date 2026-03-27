package tn.esprit.pi.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    
    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonIgnore
    private Product product;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"password", "wallet", "badges", "posts", "comments", "likes", "carts", "cars", "virtualTeams", "ownedPlayers", "rewards", "reservations", "orders", "productReviews", "coachProfile", "playerProfile", "refereeProfile", "healthProfessionalProfile", "sponsorProfile", "venueOwnerProfile", "adminProfile"})
    private User user;
}
