package tn.esprit.pi.service;

import tn.esprit.pi.dto.EvenementDTO;
import tn.esprit.pi.entity.Evenement;
import tn.esprit.pi.exception.ResourceNotFoundException;
import tn.esprit.pi.repository.EvenementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EvenementService {

    private final EvenementRepository evenementRepository;

    public List<EvenementDTO> findAll() {
        return evenementRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public EvenementDTO findById(Long id) {
        return toDTO(getOrThrow(id));
    }

    public List<EvenementDTO> findByTournoi(Long tournoiId) {
        return evenementRepository.findByTournoiId(tournoiId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    private Evenement getOrThrow(Long id) {
        return evenementRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Evenement", id));
    }

    private EvenementDTO toDTO(Evenement e) {
        return EvenementDTO.builder()
                .id(e.getId()).nom(e.getNom()).date(e.getDate())
                .lieu(e.getLieu()).description(e.getDescription())
                .type(e.getClass().getSimpleName())
                .tournoiId(e.getTournoi() != null ? e.getTournoi().getId() : null)
                .build();
    }
}