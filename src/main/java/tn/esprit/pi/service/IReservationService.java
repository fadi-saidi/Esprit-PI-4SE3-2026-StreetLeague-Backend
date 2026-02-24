package tn.esprit.pi.service;

import tn.esprit.pi.domain.Reservation;
import java.util.List;

public interface IReservationService {

    Reservation createReservation(Reservation reservation);

    Reservation updateReservation(Long id, Reservation reservation);

    Reservation getReservationById(Long id);

    List<Reservation> getAllReservations();

    void deleteReservation(Long id);
}