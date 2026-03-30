package tn.esprit.pi.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerMerch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String description;
    private Double price;
    private Integer stock;
    private String category;
    private String image;
    
    @Enumerated(EnumType.STRING)
    private SportType sportType;
    
    @Enumerated(EnumType.STRING)
    private MerchStatus status = MerchStatus.PENDING;
    
    @ManyToOne
    @JoinColumn(name = "seller_id")
    private PlayerProfile seller;
    
    @ManyToOne
    @JoinColumn(name = "approved_by")
    private User approvedBy;
    
    private LocalDateTime submittedAt;
    private LocalDateTime approvedAt;
    private String rejectionReason;
    
    // Convert to Product when approved
    @OneToOne
    @JoinColumn(name = "product_id")
    @JsonIgnore
    private Product product;
}