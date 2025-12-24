package se.gritacademy.lageruthyrningexamen.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "booking_items")
public class BookingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many items belong to one booking
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Booking booking;

    // Many items can reference same storage unit
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_unit_it", nullable = false)
    private StorageUnit storageUnit;

    @Column(name = "price_per_day", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerDay;

    public BookingItem() {}

    public BookingItem(StorageUnit storageUnit, BigDecimal pricePerDay) {
        this.storageUnit = storageUnit;
        this.pricePerDay = pricePerDay;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public StorageUnit getStorageUnit() {
        return storageUnit;
    }

    public void setStorageUnit(StorageUnit storageUnit) {
        this.storageUnit = storageUnit;
    }

    public BigDecimal getPricePerDay() {
        return pricePerDay;
    }

    public void setPricePerDay(BigDecimal pricePerDay) {
        this.pricePerDay = pricePerDay;
    }
}
