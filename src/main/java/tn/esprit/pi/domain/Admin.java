package tn.esprit.pi.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Admin extends AppUser {
    private Integer roleLevel;
}
