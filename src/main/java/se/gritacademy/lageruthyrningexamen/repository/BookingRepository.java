package se.gritacademy.lageruthyrningexamen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.gritacademy.lageruthyrningexamen.domain.Booking;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);
}
