package tn.esprit.pi.service;

import tn.esprit.pi.dto.DriverWithCarsAndCarpoolingsDTO;

import java.util.List;

public interface IAdminCarpoolingService {

    // Voir tous les drivers avec leurs voitures et carpoolings
    List<DriverWithCarsAndCarpoolingsDTO> getAllDriversWithCarsAndCarpoolings();

    // Voir un driver specifique avec ses voitures et carpoolings
    DriverWithCarsAndCarpoolingsDTO getDriverWithCarsAndCarpoolings(Long driverId);

    void deleteCar(Long carId);
    void deleteCarpooling(Long carpoolingId);
}