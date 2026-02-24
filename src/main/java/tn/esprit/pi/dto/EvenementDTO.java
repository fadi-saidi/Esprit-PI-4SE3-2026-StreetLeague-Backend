package tn.esprit.pi.dto;

import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EvenementDTO {

    private Long id;
    private String nom;
    private LocalDate date;
    private String lieu;
    private String description;
    private String type;
    private Long tournoiId;
}