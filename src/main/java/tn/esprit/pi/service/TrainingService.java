package tn.esprit.pi.service;

import tn.esprit.pi.dto.TrainingDTO;
import tn.esprit.pi.entity.Tournoi;
import tn.esprit.pi.entity.Training;
import tn.esprit.pi.exception.ResourceNotFoundException;
import tn.esprit.pi.repository.TournoiRepository;
import tn.esprit.pi.repository.TrainingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrainingService {

    private final TrainingRepository trainingRepository;
    private final TournoiRepository tournoiRepository;

    public List<TrainingDTO> findAll() {
        return trainingRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public TrainingDTO findById(Long id) {
        return toDTO(getOrThrow(id));
    }

    public List<TrainingDTO> findByTournoi(Long tournoiId) {
        return trainingRepository.findByTournoiId(tournoiId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public TrainingDTO create(TrainingDTO dto) {
        return toDTO(trainingRepository.save(toEntity(dto)));
    }

    @Transactional
    public TrainingDTO update(Long id, TrainingDTO dto) {
        Training existing = getOrThrow(id);
        existing.setNom(dto.getNom());
        existing.setDate(dto.getDate());
        existing.setLieu(dto.getLieu());
        existing.setDescription(dto.getDescription());
        existing.setCoach(dto.getCoach());
        existing.setDureeMinutes(dto.getDureeMinutes());
        existing.setObjectif(dto.getObjectif());
        existing.setTournoi(resolveTournoi(dto.getTournoiId()));
        return toDTO(trainingRepository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        if (!trainingRepository.existsById(id)) throw new ResourceNotFoundException("Training", id);
        trainingRepository.deleteById(id);
    }

    private Training getOrThrow(Long id) {
        return trainingRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Training", id));
    }

    private Tournoi resolveTournoi(Long tournoiId) {
        if (tournoiId == null) return null;
        return tournoiRepository.findById(tournoiId).orElseThrow(() -> new ResourceNotFoundException("Tournoi", tournoiId));
    }

    private TrainingDTO toDTO(Training t) {
        return TrainingDTO.builder()
                .id(t.getId()).nom(t.getNom()).date(t.getDate())
                .lieu(t.getLieu()).description(t.getDescription())
                .coach(t.getCoach()).dureeMinutes(t.getDureeMinutes()).objectif(t.getObjectif())
                .tournoiId(t.getTournoi() != null ? t.getTournoi().getId() : null)
                .build();
    }

    private Training toEntity(TrainingDTO dto) {
        Training t = new Training();
        t.setNom(dto.getNom());
        t.setDate(dto.getDate());
        t.setLieu(dto.getLieu());
        t.setDescription(dto.getDescription());
        t.setCoach(dto.getCoach());
        t.setDureeMinutes(dto.getDureeMinutes());
        t.setObjectif(dto.getObjectif());
        t.setTournoi(resolveTournoi(dto.getTournoiId()));
        return t;
    }
}