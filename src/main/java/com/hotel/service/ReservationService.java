package com.hotel.service;

import com.hotel.model.Client;
import com.hotel.model.Reservation;
import com.hotel.model.Room;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReservationService {
    private final List<Reservation> reservations;
    private final RoomService roomService;
    private final ClientService clientService;

    public ReservationService(RoomService roomService, ClientService clientService) {
        this.reservations = new ArrayList<>();
        this.roomService = roomService;
        this.clientService = clientService;
    }

    public Reservation createReservation(String clientId, String roomNumber, LocalDate checkInDate, LocalDate checkOutDate) {
        Client client = clientService.findClientById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        Room room = roomService.findRoomByNumber(roomNumber)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        if (room.isOccupied()) {
            throw new IllegalStateException("Room is already occupied");
        }

        if (checkInDate.isAfter(checkOutDate)) {
            throw new IllegalArgumentException("Check-in date must be before check-out date");
        }

        if (checkInDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in date cannot be in the past");
        }

        Reservation reservation = new Reservation(client, room, checkInDate, checkOutDate);
        reservations.add(reservation);
        return reservation;
    }

    public void cancelReservation(String reservationId) {
        findReservationById(reservationId)
                .ifPresent(Reservation::cancel);
    }

    public List<Reservation> getAllReservations() {
        return new ArrayList<>(reservations);
    }

    public List<Reservation> getActiveReservations() {
        return reservations.stream()
                .filter(reservation -> !reservation.isCancelled())
                .collect(Collectors.toList());
    }

    public Optional<Reservation> findReservationById(String reservationId) {
        return reservations.stream()
                .filter(reservation -> reservation.getId().equals(reservationId))
                .findFirst();
    }

    public List<Reservation> findReservationsByDate(LocalDate date) {
        return reservations.stream()
                .filter(reservation -> !reservation.isCancelled())
                .filter(reservation -> (
                    !date.isBefore(reservation.getCheckInDate()) &&
                    !date.isAfter(reservation.getCheckOutDate())
                ))
                .collect(Collectors.toList());
    }
}