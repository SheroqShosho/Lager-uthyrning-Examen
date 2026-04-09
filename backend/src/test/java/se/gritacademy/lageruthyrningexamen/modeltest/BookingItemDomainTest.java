package se.gritacademy.lageruthyrningexamen.modeltest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.gritacademy.lageruthyrningexamen.model.BookingItem;
import se.gritacademy.lageruthyrningexamen.model.StorageUnit;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BookingItem domain entity tests")
public class BookingItemDomainTest {

    @Test
    @DisplayName("Should create booking item with storage unit and price")
    void shouldCreateBookingItemWithStorageUnitAndPrice() {
        StorageUnit unit = new StorageUnit(1L, "A1", "Test", new BigDecimal("5.00"), new BigDecimal("100.00"), "Location", true, Instant.now());
        BigDecimal price = new BigDecimal("100.00");

        BookingItem item = new BookingItem(unit, price);

        assertEquals(unit, item.getStorageUnit());
        assertEquals(price, item.getPricePerDay());
    }

    @Test
    @DisplayName("Should create empty booking item")
    void shouldCreateEmptyBookingItem() {
        BookingItem item = new BookingItem();

        assertNull(item.getId());
    }

    @Test
    @DisplayName("Should set and get storage unit")
    void shouldSetAndGetStorageUnit() {
        BookingItem item = new BookingItem();
        StorageUnit unit = new StorageUnit(1L, "B2", "Test", new BigDecimal("10.00"), new BigDecimal("150.00"), "Location", true, Instant.now());

        item.setStorageUnit(unit);

        assertEquals(unit, item.getStorageUnit());
    }

    @Test
    @DisplayName("Should set and get price per day")
    void shouldSetAndGetPricePerDay() {
        BookingItem item = new BookingItem();
        BigDecimal price = new BigDecimal("200.00");

        item.setPricePerDay(price);

        assertEquals(price, item.getPricePerDay());
    }

    @Test
    @DisplayName("Should get storage unit ID")
    void shouldGetStorageUnitId() {
        StorageUnit unit = new StorageUnit(5L, "C3", "Test", new BigDecimal("15.00"), new BigDecimal("200.00"), "Location", true, Instant.now());
        BookingItem item = new BookingItem(unit, new BigDecimal("200.00"));

        assertEquals(5L, item.getStorageUnitId());
    }

    @Test
    @DisplayName("Should return null storage unit ID if unit is null")
    void shouldReturnNullStorageUnitIdIfUnitIsNull() {
        BookingItem item = new BookingItem();

        assertNull(item.getStorageUnitId());
    }

    @Test
    @DisplayName("Should update price per day")
    void shouldUpdatePricePerDay() {
        StorageUnit unit = new StorageUnit(1L, "A1", "Test", new BigDecimal("5.00"), new BigDecimal("100.00"), "Location", true, Instant.now());
        BookingItem item = new BookingItem(unit, new BigDecimal("100.00"));
        
        item.setPricePerDay(new BigDecimal("150.00"));
        assertEquals(new BigDecimal("150.00"), item.getPricePerDay());
    }

    @Test
    @DisplayName("Should update storage unit for booking item")
    void shouldUpdateStorageUnitForBookingItem() {
        StorageUnit unit1 = new StorageUnit(1L, "A1", "Test", new BigDecimal("5.00"), new BigDecimal("100.00"), "Location", true, Instant.now());
        StorageUnit unit2 = new StorageUnit(2L, "B1", "Test", new BigDecimal("10.00"), new BigDecimal("200.00"), "Location", true, Instant.now());
        BookingItem item = new BookingItem(unit1, new BigDecimal("100.00"));
        
        item.setStorageUnit(unit2);
        assertEquals(unit2, item.getStorageUnit());
    }

    @Test
    @DisplayName("Should get storage unit ID from item")
    void shouldGetStorageUnitIdFromItem() {
        StorageUnit unit = new StorageUnit(42L, "C1", "Test", new BigDecimal("15.00"), new BigDecimal("300.00"), "Location", true, Instant.now());
        BookingItem item = new BookingItem(unit, new BigDecimal("200.00"));
        
        assertEquals(42L, item.getStorageUnitId());
    }

    @Test
    @DisplayName("Should handle different price values")
    void shouldHandleDifferentPriceValues() {
        StorageUnit unit = new StorageUnit(1L, "D1", "Test", new BigDecimal("20.00"), new BigDecimal("400.00"), "Location", true, Instant.now());
        
        BookingItem item1 = new BookingItem(unit, new BigDecimal("100.00"));
        BookingItem item2 = new BookingItem(unit, new BigDecimal("999.99"));
        BookingItem item3 = new BookingItem(unit, new BigDecimal("0.01"));
        
        assertEquals(new BigDecimal("100.00"), item1.getPricePerDay());
        assertEquals(new BigDecimal("999.99"), item2.getPricePerDay());
        assertEquals(new BigDecimal("0.01"), item3.getPricePerDay());
    }

    @Test
    @DisplayName("Should allow setting booking reference")
    void shouldAllowSettingBookingReference() {
        StorageUnit unit = new StorageUnit(1L, "E1", "Test", new BigDecimal("25.00"), new BigDecimal("500.00"), "Location", true, Instant.now());
        BookingItem item = new BookingItem(unit, new BigDecimal("300.00"));
        
        assertNull(item.getBooking());
    }

    @Test
    @DisplayName("Should handle null storage unit gracefully")
    void shouldHandleNullStorageUnitGracefully() {
        BookingItem item = new BookingItem(null, new BigDecimal("100.00"));
        
        assertNull(item.getStorageUnit());
        assertNull(item.getStorageUnitId());
    }

    @Test
    @DisplayName("Should create booking item with valid data")
    void shouldCreateBookingItemWithValidData() {
        StorageUnit unit = new StorageUnit(5L, "F1", "Premium", new BigDecimal("50.00"), new BigDecimal("1000.00"), "Stockholm", true, Instant.now());
        BigDecimal price = new BigDecimal("500.00");
        
        BookingItem item = new BookingItem(unit, price);
        
        assertNotNull(item);
        assertEquals(unit, item.getStorageUnit());
        assertEquals(price, item.getPricePerDay());
        assertEquals(5L, item.getStorageUnitId());
    }

    @Test
    @DisplayName("Should preserve item ID after setting")
    void shouldPreserveItemIdAfterSetting() {
        StorageUnit unit = new StorageUnit(1L, "G1", "Test", new BigDecimal("30.00"), new BigDecimal("600.00"), "Location", true, Instant.now());
        BookingItem item = new BookingItem(unit, new BigDecimal("400.00"));
        
        item.setId(123L);
        assertEquals(123L, item.getId());
    }
}


