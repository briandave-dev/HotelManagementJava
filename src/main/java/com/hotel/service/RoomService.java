package com.hotel.service;

import com.hotel.model.Room;
import com.hotel.model.RoomCategory;
import com.hotel.model.Client;
import com.hotel.model.Reservation;

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

public class RoomService {
    private final List<Room> rooms;
    private final List<Reservation> reservations;

    public RoomService() {
        this.rooms = new ArrayList<>();
        this.reservations = new ArrayList<>();

    }

    public void addRoom(String number, RoomCategory category, double ratePerNight, String amenities) {
        if (findRoomByNumber(number).isPresent()) {
            throw new IllegalArgumentException("Room number already exists");
        }

        String sql = "INSERT INTO room (number, categoryDN, ratePerNight, amenities) VALUES ( ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, number);
            pstmt.setString(2, category.getDisplayName());
            pstmt.setDouble(3, ratePerNight);
            pstmt.setString(4, amenities);
            
            pstmt.executeUpdate();
            
            System.out.println("Room ajouté avec succès ! ID: " + number);
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ajout du room:");
            e.printStackTrace();
        }

        rooms.add(new Room(number, category, ratePerNight, amenities));
    }

    public void updateRoom(String number, RoomCategory category, double ratePerNight, String amenities) {
        Room room = findRoomByNumber(number)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        String sql = "UPDATE room SET categoryDN = ?, ratePerNight = ?, amenities = ? WHERE number = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            room.setCategory(category);
            room.setRatePerNight(ratePerNight);
            room.setAmenities(amenities);
            pstmt.setString(1, room.getCategory().getDisplayName());
            pstmt.setDouble(2, room.getRatePerNight());
            pstmt.setString(3, room.getAmenities());
            pstmt.setString(4, room.getNumber());
           
            pstmt.executeUpdate();
            System.out.println("Update room success");
            
        } catch (Exception e) {
            System.err.println("Error while updating room:");
            e.printStackTrace();
        }
        
    }

    public void deleteRoom(String number) {
        String sql = "DELETE FROM room WHERE number = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, number);
            
            pstmt.executeUpdate();
            System.out.println("Suppression du client réussie");
            
            rooms.removeIf(room -> room.getNumber().equals(number));
        } catch (Exception e) {
            System.err.println("Erreur lors de la suppression du client:");
            e.printStackTrace();
        }
        
    }

    public List<Room> getAllRooms() {
        rooms.clear();
        loadRoomsFromDatabase();
        return new ArrayList<>(rooms);
    }

    public List<Room> getAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate) {
        return rooms.stream()
                .filter(room -> isRoomAvailable(room, checkInDate, checkOutDate))
                .collect(Collectors.toList());
    }

    public boolean isRoomAvailable(Room room, LocalDate checkInDate, LocalDate checkOutDate) {
        return reservations.stream()
                .filter(reservation -> reservation.getRoom().equals(room))
                .filter(reservation -> !reservation.isCancelled())
                .noneMatch(reservation -> (
                    checkInDate.isBefore(reservation.getCheckOutDate()) &&
                    checkOutDate.isAfter(reservation.getCheckInDate())
                ));
    }

    public List<Room> getRoomsByCategory(RoomCategory category) {
        return rooms.stream()
                .filter(room -> room.getCategory() == category)
                .collect(Collectors.toList());
    }

    public Optional<Room> findRoomByNumber(String number) {
        return rooms.stream()
                .filter(room -> room.getNumber().equals(number))
                .findFirst();
    }

    public void addReservation(Reservation reservation) {

        String sql = "INSERT INTO reservation (id, clientId, room_number, check_in_date, check_out_date, total_price, is_cancelled) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, reservation.getId());
            pstmt.setString(2, reservation.getClient().getId());
            pstmt.setString(3, reservation.getRoom().getNumber());
            pstmt.setDate(4, Date.valueOf( reservation.getCheckInDate()));
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
    }

    private void loadRoomsFromDatabase() {
        String sql = "SELECT * FROM room";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                if(rs.getString("categoryDN").equals("Single Room")){
                Room room = new Room(
                    rs.getString("number"),
                    RoomCategory.SINGLE,
                    rs.getDouble("ratePerNight"),
                    rs.getBoolean("isOccupied"),
                    rs.getString("amenities")
                );
                rooms.add(room);
            }else  if(rs.getString("categoryDN").equals("Double Room")){
                Room room = new Room(
                    rs.getString("number"),
                    RoomCategory.DOUBLE,
                    rs.getDouble("ratePerNight"),
                    rs.getBoolean("isOccupied"),
                    rs.getString("amenities")
                );
                rooms.add(room);
            }else {
                Room room = new Room(
                    rs.getString("number"),
                    RoomCategory.SUITE,
                    rs.getDouble("ratePerNight"),
                    rs.getBoolean("isOccupied"),
                    rs.getString("amenities")
                );
                rooms.add(room);
            }
                
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des clients depuis la base de données:");
            e.printStackTrace();
        }
    }

    public static void craeteRoomTableIfNotExists() {
        String sql = "Create TABLE IF NOT EXISTS category("+
                     "displayName Varchar(20) PRIMARY KEY,"+
                     "capacity integer);";
         String[] insertQueries = {
            "INSERT INTO category(displayName, capacity) VALUES ('Single Room', 1) ON DUPLICATE KEY UPDATE capacity = VALUES(capacity);",
            "INSERT INTO category(displayName, capacity) VALUES ('Double Room', 2) ON DUPLICATE KEY UPDATE capacity = VALUES(capacity);",
            "INSERT INTO category(displayName, capacity) VALUES ('Suite', 4) ON DUPLICATE KEY UPDATE capacity = VALUES(capacity);"
        };
        String sql1 = "CREATE TABLE IF NOT EXISTS room (" +
                     "number Varchar(20) PRIMARY KEY," +
                     "categoryDN VARCHAR(20) NOT NULL," +
                     "ratePerNight Double NOT NULL," +
                     "isOccupied boolean NOT NULL default false," +
                     "amenities VARCHAR(20) NOT NULL," +
                     "FOREIGN KEY (categoryDN) REFERENCES category(displayName) ON DELETE CASCADE);";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate(sql);
            for (String insertQuery : insertQueries) {
                stmt.executeUpdate(insertQuery);  // Exécuter chaque requête séparément
            }
            System.out.println("Table 'category' prête !");
            stmt.executeUpdate(sql1);
            System.out.println("Table 'room' prête !");
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de la table reservation:");
            e.printStackTrace();
        }
    }
}