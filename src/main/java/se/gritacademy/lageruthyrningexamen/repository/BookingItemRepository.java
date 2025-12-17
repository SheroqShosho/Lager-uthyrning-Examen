package se.gritacademy.lageruthyrningexamen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.gritacademy.lageruthyrningexamen.domain.BookingItem;

public interface BookingItemRepository extends JpaRepository<BookingItem, Long> {
}
