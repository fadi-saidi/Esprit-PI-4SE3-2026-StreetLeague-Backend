package tn.esprit.pi.service;

import tn.esprit.pi.dto.MatchDTO;
import tn.esprit.pi.entity.Match;
import tn.esprit.pi.entity.Tournoi;
import tn.esprit.pi.exception.RessourceNotFoundException;
import tn.esprit.pi.repository.MatchRepository;
import tn.esprit.pi.repository.TournoiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchService {

    private final MatchRepository matchRepository;
    private final TournoiRepository tournoiRepository;

    public List<MatchDTO> findAll() {
        return matchRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public MatchDTO findById(Long id) {
        return toDTO(getOrThrow(id));
    }

    public List<MatchDTO> findByTournoi(Long tournoiId) {
        return matchRepository.findByTournoiId(tournoiId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public MatchDTO create(MatchDTO dto) {
        return toDTO(matchRepository.save(toEntity(dto)));
    }

    @Transactional
    public MatchDTO update(Long id, MatchDTO dto) {
        Match existing = getOrThrow(id);
        existing.setNom(dto.getNom());
        existing.setDate(dto.getDate());
        existing.setLieu(dto.getLieu());
        existing.setDescription(dto.getDescription());
        existing.setEquipeA(dto.getEquipeA());
        existing.setEquipeB(dto.getEquipeB());
        existing.setScoreA(dto.getScoreA());
        existing.setScoreB(dto.getScoreB());
        existing.setTournoi(resolveTournoi(dto.getTournoiId()));
        return toDTO(matchRepository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        if (!matchRepository.existsById(id))
            throw new RessourceNotFoundException("Match", id);
        matchRepository.deleteById(id);
    }

    private Match getOrThrow(Long id) {
        return matchRepository.findById(id).orElseThrow(() -> new RessourceNotFoundException("Match", id));
    }

    private Tournoi resolveTournoi(Long tournoiId) {
        if (tournoiId == null)
            return null;
        return tournoiRepository.findById(tournoiId)
                .orElseThrow(() -> new RessourceNotFoundException("Tournoi", tournoiId));
    }

    private MatchDTO toDTO(Match m) {
        return MatchDTO.builder()
                .id(m.getId()).nom(m.getNom()).date(m.getDate())
                .lieu(m.getLieu()).description(m.getDescription())
                .equipeA(m.getEquipeA()).equipeB(m.getEquipeB())
                .scoreA(m.getScoreA()).scoreB(m.getScoreB())
                .tournoiId(m.getTournoi() != null ? m.getTournoi().getId() : null)
                .build();
    }

    private Match toEntity(MatchDTO dto) {
        Match m = new Match();
        m.setNom(dto.getNom());
        m.setDate(dto.getDate());
        m.setLieu(dto.getLieu());
        m.setDescription(dto.getDescription());
        m.setEquipeA(dto.getEquipeA());
        m.setEquipeB(dto.getEquipeB());
        m.setScoreA(dto.getScoreA());
        m.setScoreB(dto.getScoreB());
        m.setTournoi(resolveTournoi(dto.getTournoiId()));
        return m;
    }
}