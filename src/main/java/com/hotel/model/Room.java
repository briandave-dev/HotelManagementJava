package com.hotel.model;

public class Room {
    private final String number;
    private RoomCategory category;
    private double ratePerNight;
    private boolean isOccupied;
    private String amenities;

    public Room(String number, RoomCategory category, double ratePerNight, String amenities) {
        this.number = number;
        this.category = category;
        this.ratePerNight = ratePerNight;
        this.amenities = amenities;
        this.isOccupied = false;
    }

    public String getNumber() {
        return number;
    }

    public RoomCategory getCategory() {
        return category;
    }

    public void setCategory(RoomCategory category) {
        this.category = category;
    }

    public double getRatePerNight() {
        return ratePerNight;
    }

    public void setRatePerNight(double ratePerNight) {
        this.ratePerNight = ratePerNight;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public void setOccupied(boolean occupied) {
        isOccupied = occupied;
    }

    public String getAmenities() {
        return amenities;
    }

    public void setAmenities(String amenities) {
        this.amenities = amenities;
    }

    @Override
    public String toString() {
        return "Room " + number + " (" + category + ")";
    }
}