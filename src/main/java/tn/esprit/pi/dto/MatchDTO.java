package tn.esprit.pi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MatchDTO {

    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotNull(message = "La date est obligatoire")
    private LocalDate date;

    @NotBlank(message = "Le lieu est obligatoire")
    private String lieu;

    private String description;

    @NotBlank(message = "L'équipe A est obligatoire")
    private String equipeA;

    @NotBlank(message = "L'équipe B est obligatoire")
    private String equipeB;

    @Min(value = 0, message = "Le score ne peut pas être négatif")
    @NotNull(message = "Le score A est obligatoire")
    private Integer scoreA;

    @Min(value = 0, message = "Le score ne peut pas être négatif")
    @NotNull(message = "Le score B est obligatoire")
    private Integer scoreB;

    private Long tournoiId;
}
