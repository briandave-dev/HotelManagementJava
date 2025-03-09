package com.hotel.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class Reservation {
    private final String id;
    private final Client client;
    private final Room room;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private final double totalPrice;
    private boolean isCancelled;

    public Reservation(Client client, Room room, LocalDate checkInDate, LocalDate checkOutDate) {
        this.id = UUID.randomUUID().toString();
        this.client = client;
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.totalPrice = calculateTotalPrice();
        this.isCancelled = false;
        
        room.setOccupied(true);
        client.addReservation(this);
    }

    private double calculateTotalPrice() {
        long numberOfNights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        return numberOfNights * room.getRatePerNight();
    }

    public String getId() {
        return id;
    }

    public Client getClient() {
        return client;
    }

    public Room getRoom() {
        return room;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void cancel() {
        if (!isCancelled) {
            this.isCancelled = true;
            room.setOccupied(false);
        }
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    @Override
    public String toString() {
        return String.format("Reservation for %s - Room %s (%s to %s)", 
            client.toString(), room.getNumber(), checkInDate, checkOutDate);
    }
}