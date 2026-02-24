package tn.esprit.pi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TrainingDTO {

    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotNull(message = "La date est obligatoire")
    private LocalDate date;

    @NotBlank(message = "Le lieu est obligatoire")
    private String lieu;

    private String description;

    @NotBlank(message = "Le coach est obligatoire")
    private String coach;

    @Min(value = 1, message = "La durée doit être positive")
    @NotNull(message = "La durée est obligatoire")
    private Integer dureeMinutes;

    private String objectif;

    private Long tournoiId;
}