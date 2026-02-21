package tn.esprit.pi.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private LocalDateTime createdAt;
    private Double totalAmount;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser user;
    
    @ManyToMany
    @JoinTable(name = "cart_product",
        joinColumns = @JoinColumn(name = "cart_id"),
        inverseJoinColumns = @JoinColumn(name = "product_id"))
    private Set<Product> products;
}
