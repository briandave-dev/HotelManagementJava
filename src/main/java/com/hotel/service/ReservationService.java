package com.hotel.service;

import com.hotel.model.Client;
import com.hotel.model.Reservation;
import com.hotel.model.Room;
import com.hotel.model.RoomCategory;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
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

        String sql = "INSERT INTO reservation (id, clientId, room_number, check_in_date, check_out_date, total_price, is_cancelled) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, reservation.getId());
            pstmt.setString(2, reservation.getClient().getId());
            pstmt.setString(3, reservation.getRoom().getNumber());
            pstmt.setDate(4, Date.valueOf(reservation.getCheckInDate()));
            pstmt.setDate(5, Date.valueOf(reservation.getCheckOutDate()));
            pstmt.setDouble(6, reservation.getTotalPrice());
            pstmt.setBoolean(7, reservation.isCancelled());
            
            pstmt.executeUpdate();
            
            System.out.println("Reservation ajouté avec succès ! ID: " + reservation.getId());
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ajout de la reservation:");
            e.printStackTrace();
        }
        reservations.add(reservation);
        return reservation;
    }

    public void cancelReservation(String reservationId) {
        findReservationById(reservationId)
                .ifPresent(Reservation::cancel);
        String sql = "UPDATE reservation SET is_cancelled = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, findReservationById(reservationId).get().isCancelled() );
            pstmt.setString(2, findReservationById(reservationId).get().getId());
           
            pstmt.executeUpdate();
            System.out.println("Update reservation cancelled success");
            
        } catch (Exception e) {
            System.err.println("Error while updating cancelled reservation:");
            e.printStackTrace();
        }
    }

    public List<Reservation> getAllReservations() {
        reservations.clear();
        loadReserationFromDatabase();
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

    public boolean isRoomAvailable(String roomNumber, LocalDate checkInDate, LocalDate checkOutDate) {
        return isRoomAvailable(roomNumber, checkInDate, checkOutDate, null);
    }

    public boolean isRoomAvailable(String roomNumber, LocalDate checkInDate, LocalDate checkOutDate, String excludeReservationId) {
        return reservations.stream()
                .filter(reservation -> !reservation.isCancelled())
                .filter(reservation -> excludeReservationId == null || !reservation.getId().equals(excludeReservationId))
                .filter(reservation -> reservation.getRoom().getNumber().equals(roomNumber))
                .noneMatch(reservation -> (
                    checkInDate.isBefore(reservation.getCheckOutDate()) &&
                    checkOutDate.isAfter(reservation.getCheckInDate())
                ));
    }

    public void updateReservationDates(String reservationId, LocalDate newCheckInDate, LocalDate newCheckOutDate) {
        Reservation reservation = findReservationById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        if (reservation.isCancelled()) {
            throw new IllegalStateException("Cannot update cancelled reservation");
        }

        if (newCheckInDate.isAfter(newCheckOutDate)) {
            throw new IllegalArgumentException("Check-in date must be before check-out date");
        }

        if (newCheckInDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in date cannot be in the past");
        }
        String sql = "UPDATE reservation SET check_in_date = ?, check_out_date = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(newCheckInDate) );
            pstmt.setDate(2, Date.valueOf(newCheckOutDate));
            pstmt.setString(3, reservationId);
           
            pstmt.executeUpdate();
            System.out.println("Update reservation success");
            
        } catch (Exception e) {
            System.err.println("Error while updating reservation:");
            e.printStackTrace();
        }
        reservation.setCheckInDate(newCheckInDate);
        reservation.setCheckOutDate(newCheckOutDate);
    }

      private void loadReserationFromDatabase() {
        String sql = "SELECT * FROM reservation";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Date sqlCheckInDate = rs.getDate("check_in_date"); 
                Date sqlCheckOutDate = rs.getDate("check_out_date"); 
                Reservation reservation = new Reservation(
                    rs.getString("id"),
                    clientService.findClientById( rs.getString("clientId")).get(),
                    roomService.findRoomByNumber( rs.getString("room_number")).get(),
                    sqlCheckInDate.toLocalDate(),
                    sqlCheckOutDate.toLocalDate(),
                    rs.getDouble("total_price"),
                    rs.getBoolean("is_cancelled")
                );
                reservations.add(reservation);
            
                
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des reservations depuis la base de données:");
            e.printStackTrace();
        }
    }


     public static void createReservationTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS reservation (" +
                     "id VARCHAR(50) PRIMARY KEY," +
                     "clientId VARCHAR(50) NOT NULL," +
                     "room_number VARCHAR(20) NOT NULL," +
                     "check_in_date DATE NOT NULL," +
                     "check_out_date DATE NOT NULL," +
                     "total_price DOUBLE NOT NULL," +
                     "is_cancelled BOOLEAN NOT NULL DEFAULT FALSE," +
                     "FOREIGN KEY (clientId) REFERENCES client(id) ON DELETE CASCADE," +
                     "FOREIGN KEY (room_number) REFERENCES room(number) ON DELETE CASCADE)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate(sql);
            System.out.println("Table 'reservation' prête !");
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de la table reservation:");
            e.printStackTrace();
        }
    }

}