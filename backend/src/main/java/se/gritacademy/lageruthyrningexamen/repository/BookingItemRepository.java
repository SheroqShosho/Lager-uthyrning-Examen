package se.gritacademy.lageruthyrningexamen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.gritacademy.lageruthyrningexamen.model.BookingItem;

public interface BookingItemRepository extends JpaRepository<BookingItem, Long> {
}
