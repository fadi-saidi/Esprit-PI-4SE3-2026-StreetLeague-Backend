package tn.esprit.pi.service;

import tn.esprit.pi.dto.CarpoolingDTO;

import java.util.List;

public interface ICarpoolingService {

    // Driver : creer un trajet avec sa voiture
    CarpoolingDTO createCarpooling(CarpoolingDTO dto, String email);

    // Voir tous les trajets disponibles
    List<CarpoolingDTO> getAllCarpoolings();

    // Voir mes trajets crees (en tant que driver)
    List<CarpoolingDTO> getMyCreatedCarpoolings(String email);

    // Voir les trajets auxquels je participe
    List<CarpoolingDTO> getMyJoinedCarpoolings(String email);

    // Voir le detail d'un trajet
    CarpoolingDTO getCarpoolingById(Long carpoolingId);

    // Rejoindre un trajet
    CarpoolingDTO joinCarpooling(Long carpoolingId, String email);

    // Quitter un trajet
    CarpoolingDTO leaveCarpooling(Long carpoolingId, String email);

    // Driver : supprimer son trajet
    void deleteCarpooling(Long carpoolingId, String email);
}