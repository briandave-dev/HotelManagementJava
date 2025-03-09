package com.hotel.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Client {
    private final String id;
    private String firstName;
    private String lastName;
    private String address;
    private String phoneNumber;
    private String email;
    private List<Reservation> reservationHistory;

    public Client(String firstName, String lastName, String address, String phoneNumber, String email) {
        this.id = UUID.randomUUID().toString();
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.reservationHistory = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Reservation> getReservationHistory() {
        return new ArrayList<>(reservationHistory);
    }

    public void addReservation(Reservation reservation) {
        this.reservationHistory.add(reservation);
    }

    @Override
    public String toString() {
        return firstName + " " + lastName;
    }
}