package tn.esprit.pi.dto;

import tn.esprit.pi.domain.Role;

public class Dtos {

        public record RegisterRequest(
                        String fullName,
                        String email,
                        String password,
                        Role role,
                        String dateOfBirth,
                        String certificate,
                        String licenseNumber,
                        String specialty,
                        Integer experienceYears,

                        String companyName,
                        String logo,
                        String contactEmail,
                        Double budget,

                        String phone) {
        }

        public record LoginRequest(
                        String email,
                        String password) {
        }

        public record AuthResponse(
                        String token,
                        String email,
                        String role) {
        }
}
