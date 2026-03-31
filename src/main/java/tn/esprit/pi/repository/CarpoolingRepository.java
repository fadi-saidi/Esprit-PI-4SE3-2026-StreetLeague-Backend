package tn.esprit.pi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pi.domain.Carpooling;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CarpoolingRepository extends JpaRepository<Carpooling, Long> {

    // Trajets crees par le driver (via la voiture)
    List<Carpooling> findByCar_DriverId(Long driverId);

    // Trajets auxquels un user participe
    List<Carpooling> findByParticipants_Id(Long userId);

    // Trajets expirés (date dépassée)
    List<Carpooling> findByDateBefore(LocalDate date);
}