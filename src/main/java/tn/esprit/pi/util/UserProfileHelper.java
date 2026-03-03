package tn.esprit.pi.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.repository.*;

import java.util.Optional;

@Component
public class UserProfileHelper {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CoachProfileRepository coachProfileRepository;
    @Autowired
    private PlayerProfileRepository playerProfileRepository;
    @Autowired
    private RefereeProfileRepository refereeProfileRepository;
    @Autowired
    private HealthProfessionalProfileRepository healthProfessionalProfileRepository;
    @Autowired
    private SponsorProfileRepository sponsorProfileRepository;
    @Autowired
    private VenueOwnerProfileRepository venueOwnerProfileRepository;
    @Autowired
    private AdminProfileRepository adminProfileRepository;

    public Object getProfileByUser(User user) {
        return switch (user.getRole()) {
            case COACH -> coachProfileRepository.findByUserId(user.getId()).orElse(null);
            case PLAYER -> playerProfileRepository.findByUserId(user.getId()).orElse(null);
            case REFEREE -> refereeProfileRepository.findByUserId(user.getId()).orElse(null);
            case HEALTH_PROFESSIONAL -> healthProfessionalProfileRepository.findByUserId(user.getId()).orElse(null);
            case SPONSOR -> sponsorProfileRepository.findByUserId(user.getId()).orElse(null);
            case VENUE_OWNER -> venueOwnerProfileRepository.findByUserId(user.getId()).orElse(null);
            case ADMIN -> adminProfileRepository.findByUserId(user.getId()).orElse(null);
        };
    }

    public Optional<CoachProfile> getCoachProfile(Long userId) {
        return coachProfileRepository.findByUserId(userId);
    }

    public Optional<PlayerProfile> getPlayerProfile(Long userId) {
        return playerProfileRepository.findByUserId(userId);
    }

    public Optional<RefereeProfile> getRefereeProfile(Long userId) {
        return refereeProfileRepository.findByUserId(userId);
    }

    public boolean hasRole(User user, Role role) {
        return user.getRole() == role;
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
