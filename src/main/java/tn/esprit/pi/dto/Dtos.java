package tn.esprit.pi.dto;

import tn.esprit.pi.domain.Role;

public class Dtos {

    public record RegisterRequest(

            String fullName,
            String email,
            String password,
            Role role,
            // Player
            String dateOfBirth,
            // Health Professional, Referee, Coach
            String certificate,
            String licenseNumber,
            String specialty,
            Integer experienceYears,
            // Sponsor
            String companyName,
            String logo,
            String contactEmail,
            Double budget,
            // Venue Owner
            String phone
    ) {}

    public record LoginRequest(
            String email,
            String password
    ) {}

    public record AuthResponse(

            String token,
            String email,
            String role
    ) {}
}
