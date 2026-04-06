package tn.esprit.pi.gestiontournoi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import tn.esprit.pi.domain.User;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.DecisionRequest;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.EventRequest;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.EventResponse;
import tn.esprit.pi.gestiontournoi.entity.ApprovalStatus;
import tn.esprit.pi.gestiontournoi.entity.GestionEvent;
import tn.esprit.pi.gestiontournoi.repository.GestionEventRepository;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GestionEventService {

    private final GestionEventRepository repository;
    private final GestionModuleSupport support;

    public List<EventResponse> getApproved(Authentication authentication) {
        User currentUser = support.getCurrentUser(authentication);
        return repository.findAll().stream()
                .filter(event -> support.getEffectiveStatus(event) == ApprovalStatus.APPROVED)
                .sorted(Comparator
                        .comparing(GestionEvent::getDate, Comparator.nullsLast(String::compareTo))
                        .thenComparing(GestionEvent::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(event -> toResponse(event, currentUser))
                .toList();
    }

    public List<EventResponse> getRequests(Authentication authentication) {
        User admin = support.requireAuthenticatedUser(authentication);
        support.requireAdmin(admin);
        return repository.findAll().stream()
                .sorted(Comparator
                        .comparing(GestionEvent::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(GestionEvent::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(event -> toResponse(event, admin))
                .toList();
    }

    public EventResponse submit(EventRequest request, Authentication authentication) {
        User requester = support.requireAuthenticatedUser(authentication);

        GestionEvent event = new GestionEvent();
        applyRequest(event, request);
        support.markPendingRequest(event, requester);

        return toResponse(repository.save(event), requester);
    }

    public EventResponse update(Long id, EventRequest request, Authentication authentication) {
        User admin = support.requireAuthenticatedUser(authentication);
        support.requireAdmin(admin);

        GestionEvent event = findById(id);
        applyRequest(event, request);
        return toResponse(repository.save(event), admin);
    }

    public void delete(Long id, Authentication authentication) {
        User admin = support.requireAuthenticatedUser(authentication);
        support.requireAdmin(admin);

        GestionEvent event = findById(id);
        repository.delete(event);
    }

    public EventResponse approve(Long id, DecisionRequest request, Authentication authentication) {
        User admin = support.requireAuthenticatedUser(authentication);
        support.requireAdmin(admin);

        GestionEvent event = findById(id);
        support.markDecision(event, ApprovalStatus.APPROVED, admin, request == null ? null : request.note());
        return toResponse(repository.save(event), admin);
    }

    public EventResponse reject(Long id, DecisionRequest request, Authentication authentication) {
        User admin = support.requireAuthenticatedUser(authentication);
        support.requireAdmin(admin);

        GestionEvent event = findById(id);
        support.markDecision(event, ApprovalStatus.REJECTED, admin, request == null ? null : request.note());
        return toResponse(repository.save(event), admin);
    }

    public EventResponse participate(Long id, Authentication authentication) {
        User user = support.requireAuthenticatedUser(authentication);

        GestionEvent event = findById(id);
        if (support.getEffectiveStatus(event) != ApprovalStatus.APPROVED) {
            throw new IllegalArgumentException("Only approved events can accept participants");
        }

        event.getParticipantUserIds().add(user.getId());
        return toResponse(repository.save(event), user);
    }

    public EventResponse cancelParticipation(Long id, Authentication authentication) {
        User user = support.requireAuthenticatedUser(authentication);

        GestionEvent event = findById(id);
        event.getParticipantUserIds().remove(user.getId());
        return toResponse(repository.save(event), user);
    }

    private GestionEvent findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
    }

    private void applyRequest(GestionEvent event, EventRequest request) {
        event.setName(support.requireText(request.name(), "Le nom de l evenement", 3, 100));
        event.setDescription(support.normalizeOptionalTextWithLimit(request.description(), 500, "La description de l evenement"));
        event.setDate(support.requireIsoDate(request.date(), "La date de l evenement"));
        event.setLocation(support.requireText(request.location(), "Le lieu de l evenement", 3, 120));
    }

    private EventResponse toResponse(GestionEvent event, User currentUser) {
        boolean participating = currentUser != null && event.getParticipantUserIds().contains(currentUser.getId());
        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getDate(),
                event.getLocation(),
                support.getEffectiveStatus(event),
                support.toUserSummary(event.getRequesterUserId(), event.getRequesterName(), event.getRequesterEmail()),
                support.toUserSummary(event.getApprovedByUserId(), event.getApprovedByName(), event.getApprovedByEmail()),
                event.getApprovalNote(),
                event.getParticipantUserIds().size(),
                participating
        );
    }
}
