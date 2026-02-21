package tn.esprit.pi.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private Double price;
    private Integer stock;
    private String category;
    
    @Enumerated(EnumType.STRING)
    private SportType sportType;
    
    @ManyToMany(mappedBy = "products")
    private Set<Cart> carts;
}
