package tn.esprit.pi.service;

import tn.esprit.pi.dto.CarDTO;

import java.util.List;

public interface ICarService {

    // Ajouter ma voiture
    CarDTO addCar(CarDTO dto, String email);

    // Voir toutes les voitures disponibles
    List<CarDTO> getAllCars();

    // Voir mes voitures
    List<CarDTO> getMyCars(String email);

    // Voir une voiture par ID
    CarDTO getCarById(Long carId);

    // Supprimer ma voiture
    void deleteCar(Long carId, String email);

    // Mettre à jour la photo de la voiture
    CarDTO updatePhotoUrl(Long carId, String photoUrl, String email);
}