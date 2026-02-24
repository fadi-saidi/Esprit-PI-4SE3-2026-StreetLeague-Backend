package tn.esprit.pi.service;

import tn.esprit.pi.dto.TournoiDTO;
import tn.esprit.pi.entity.Tournoi;
import tn.esprit.pi.exception.RessourceNotFoundException;
import tn.esprit.pi.repository.TournoiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TournoiService {

    private final TournoiRepository tournoiRepository;

    public List<TournoiDTO> findAll() {
        return tournoiRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public TournoiDTO findById(Long id) {
        return toDTO(getOrThrow(id));
    }

    @Transactional
    public TournoiDTO create(TournoiDTO dto) {
        return toDTO(tournoiRepository.save(toEntity(dto)));
    }

    @Transactional
    public TournoiDTO update(Long id, TournoiDTO dto) {
        Tournoi existing = getOrThrow(id);
        existing.setNom(dto.getNom());
        existing.setDateDebut(dto.getDateDebut());
        existing.setDateFin(dto.getDateFin());
        existing.setLieu(dto.getLieu());
        return toDTO(tournoiRepository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        if (!tournoiRepository.existsById(id)) throw new RessourceNotFoundException("Tournoi", id);
        tournoiRepository.deleteById(id);
    }

    private Tournoi getOrThrow(Long id) {
        return tournoiRepository.findById(id).orElseThrow(() -> new RessourceNotFoundException("Tournoi", id));
    }

    private TournoiDTO toDTO(Tournoi t) {
        return TournoiDTO.builder().id(t.getId()).nom(t.getNom())
                .dateDebut(t.getDateDebut()).dateFin(t.getDateFin()).lieu(t.getLieu()).build();
    }

    private Tournoi toEntity(TournoiDTO dto) {
        return Tournoi.builder().nom(dto.getNom())
                .dateDebut(dto.getDateDebut()).dateFin(dto.getDateFin()).lieu(dto.getLieu()).build();
    }
}