package com.hotel.service;

import com.hotel.model.Client;
import com.hotel.model.Reservation;
import com.hotel.model.Room;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClientService {
    private final List<Client> clients;
    private final RoomService roomService;

    public ClientService(RoomService roomService) {
        this.clients = new ArrayList<>();
        this.roomService = roomService;
        createClientsTableIfNotExists();
        RoomService.craeteRoomTableIfNotExists();
        ReservationService.createReservationTableIfNotExists();
        InvoiceService.craeteInvoiceTableIfNotExists();
        loadClientsFromDatabase();
    }

    // Méthode pour charger tous les clients depuis la base de données 
    private void loadClientsFromDatabase() {
        String sql = "SELECT * FROM client";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Client client = new Client(
                    rs.getString("id"),
                    rs.getString("firstName"),
                    rs.getString("lastName"),
                    rs.getString("address"),
                    rs.getString("phoneNumber"),
                    rs.getString("email")
                );
                clients.add(client);
                
                // Charger les réservations associées à ce client
                loadClientReservations(client);
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des clients depuis la base de données:");
            e.printStackTrace();
        }
    }
    
    // Méthode pour charger les réservations d'un client
    private void loadClientReservations(Client client) {
        String sql = "SELECT * FROM reservation WHERE clientId = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, client.getId());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String roomNumber = rs.getString("room_number");
                    Optional<Room> roomOpt = roomService.findRoomByNumber(roomNumber);
                    
                    if (roomOpt.isPresent()) {
                        Room room = roomOpt.get();
                        LocalDate checkInDate = rs.getDate("check_in_date").toLocalDate();
                        LocalDate checkOutDate = rs.getDate("check_out_date").toLocalDate();
                        boolean isCancelled = rs.getBoolean("is_cancelled");
                        
                        Reservation reservation = new Reservation(client, room, checkInDate, checkOutDate);
                        client.addReservation(reservation);
                        if (isCancelled) {
                            reservation.cancel();
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des réservations du client:");
            e.printStackTrace();
        }
    }

    public void addClient(String firstName, String lastName, String address, String phoneNumber, String email) {
        Client c = new Client(firstName, lastName, address, phoneNumber, email);
        clients.add(c);
        String sql = "INSERT INTO client (id, firstName, lastName, address, phoneNumber, email) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, c.getId());
            pstmt.setString(2, c.getFirstName());
            pstmt.setString(3, c.getLastName());
            pstmt.setString(4, c.getAddress());
            pstmt.setString(5, c.getPhoneNumber());
            pstmt.setString(6, c.getEmail());
            
            pstmt.executeUpdate();
            
            System.out.println("Client ajouté avec succès ! ID: " + c.getId());
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ajout du client:");
            e.printStackTrace();
        }
    }

    public void updateClient(String clientId, String firstName, String lastName, String address, String phoneNumber, String email) {
        Client client = findClientById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));
        
        client.setFirstName(firstName);
        client.setLastName(lastName);
        client.setAddress(address);
        client.setPhoneNumber(phoneNumber);
        client.setEmail(email);
        
        String sql = "UPDATE client SET firstName = ?, lastName = ?, address = ?, phoneNumber = ?, email = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, client.getFirstName());
            pstmt.setString(2, client.getLastName());
            pstmt.setString(3, client.getAddress());
            pstmt.setString(4, client.getPhoneNumber());
            pstmt.setString(5, client.getEmail());
            pstmt.setString(6, client.getId());
            
            pstmt.executeUpdate();
            System.out.println("Mise à jour du client réussie");
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour du client:");
            e.printStackTrace();
        }
    }

    public void deleteClient(String clientId) {
        
        String sql = "DELETE FROM client WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, clientId);
            
            pstmt.executeUpdate();
            System.out.println("Suppression du client réussie");
            
            // Ensuite, supprimer de la liste en mémoire
            clients.removeIf(client -> client.getId().equals(clientId));
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la suppression du client:");
            e.printStackTrace();
        }
    }

    public List<Client> getAllClients() {
        // Recharger les clients depuis la base de données pour avoir les données à jour
        clients.clear();
        loadClientsFromDatabase();
        return new ArrayList<>(clients);
    }

    public Optional<Client> findClientById(String clientId) {
        // D'abord, essayer de trouver dans la liste en mémoire
        Optional<Client> clientOptional = clients.stream()
                .filter(client -> client.getId().equals(clientId))
                .findFirst();
        
        // Si non trouvé, chercher dans la base de données
        if (clientOptional.isEmpty()) {
            String sql = "SELECT * FROM client WHERE id = ?";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, clientId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        Client client = new Client(
                            rs.getString("id"),
                            rs.getString("firstName"),
                            rs.getString("lastName"),
                            rs.getString("address"),
                            rs.getString("phoneNumber"),
                            rs.getString("email")
                        );
                        
                        // Charger les réservations associées
                        loadClientReservations(client);
                        
                        clients.add(client); // Ajouter à la liste en mémoire
                        return Optional.of(client);
                    }
                }
                
            } catch (Exception e) {
                System.err.println("Erreur lors de la recherche du client par ID:");
                e.printStackTrace();
            }
        }
        
        return clientOptional;
    }

    public List<Reservation> getClientReservations(String clientId) {
        Optional<Client> clientOpt = findClientById(clientId);
        
        if (clientOpt.isPresent()) {
            Client client = clientOpt.get();
            
            // Récupérer directement depuis la base de données
            String sql = "SELECT * FROM reservation WHERE clientId = ?";
            List<Reservation> reservations = new ArrayList<>();
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, clientId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String roomNumber = rs.getString("room_number");
                        Optional<Room> roomOpt = roomService.findRoomByNumber(roomNumber);
                        
                        if (roomOpt.isPresent()) {
                            Room room = roomOpt.get();
                            LocalDate checkInDate = rs.getDate("check_in_date").toLocalDate();
                            LocalDate checkOutDate = rs.getDate("check_out_date").toLocalDate();
                            boolean isCancelled = rs.getBoolean("is_cancelled");
                            
                            Reservation reservation = new Reservation(client, room, checkInDate, checkOutDate);
                            if (isCancelled) {
                                reservation.cancel();
                            }
                            
                            reservations.add(reservation);
                        }
                    }
                }
                
                return reservations;
                
            } catch (Exception e) {
                System.err.println("Erreur lors de la récupération des réservations du client:");
                e.printStackTrace();
            }
        }
        
        return new ArrayList<>();
    }

    public void createClientsTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS client (" +
                     "id VARCHAR(50) PRIMARY KEY," +
                     "firstName VARCHAR(100) NOT NULL," +
                     "lastName VARCHAR(100)," +
                     "address VARCHAR(255)," +
                     "phoneNumber VARCHAR(20)," +
                     "email VARCHAR(100) NOT NULL UNIQUE)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate(sql);
            System.out.println("Table 'client' prête !");
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de la table client:");
            e.printStackTrace();
        }
    }
}