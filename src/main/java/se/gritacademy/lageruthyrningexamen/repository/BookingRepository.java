package se.gritacademy.lageruthyrningexamen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import se.gritacademy.lageruthyrningexamen.domain.Booking;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);

    @Query("""
        select count(b) > 0 from Booking b
        join b.items i
        where i.storageUnit.id = :unitId
          and b.status in ('PENDING', 'PAID')
          and b.startDate <= :endDate
          and b.endDate >= :startDate
    """)
    boolean existsOverlappingBookingForUnit(
            @Param("unitId") Long unitId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
