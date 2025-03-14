package com.hotel.service;

import com.hotel.model.AdditionalService;
import com.hotel.model.Client;
import com.hotel.model.Invoice;
import com.hotel.model.Reservation;
import com.hotel.model.Room;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.JOptionPane;

public class InvoiceService {
    private final List<Invoice> invoices;
    private final double defaultTaxRate;

    public InvoiceService(double defaultTaxRate) {
        this.invoices = new ArrayList<>();
        this.defaultTaxRate = defaultTaxRate;
    }

    public Invoice generateInvoice(Reservation reservation) {
        Invoice invoice = new Invoice(reservation, defaultTaxRate);
        String sql = "INSERT INTO invoice (id, reservationId, generationDate, subtotal, tax, total) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ) {
            
            pstmt.setString(1, invoice.getId());
            pstmt.setString(2, invoice.getReservation().getId());
            pstmt.setTimestamp(3, Timestamp.valueOf( invoice.getGenerationDate()));
            pstmt.setDouble(4, invoice.getSubtotal());
            pstmt.setDouble(5, invoice.getTax());
            pstmt.setDouble(6, invoice.getTotal());
            
            pstmt.executeUpdate();

           System.out.println("Invoice ajouté avec succès ! ID: " + invoice.getId());
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ajout de l'invoice:");
            e.printStackTrace();
        }
        invoices.add(invoice);
        return invoice;
    }

    public void addAdditionalService(String invoiceId, String description, double price) {
        findInvoiceById(invoiceId)
                .ifPresent(invoice -> invoice.addAdditionalService(description, price));
        String sql = "INSERT INTO additionalService (invoiceId, description, price) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ) {
            
            pstmt.setString(1, invoiceId);
            pstmt.setString(2, description);
            pstmt.setDouble(3, price);
         
            pstmt.executeUpdate();

           System.out.println("additionalService ajouté avec succès ! ID: ");
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ajout de l'invoice:");
            e.printStackTrace();
        }
    }

    public void markAsPaid(String invoiceId) {
        findInvoiceById(invoiceId)
                .ifPresent(invoice -> invoice.setPaid(true));
        String sql = "UPDATE invoice SET isPaid = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, findInvoiceById(invoiceId).get().isPaid() );
            pstmt.setString(2, invoiceId);
           
            pstmt.executeUpdate();
            System.out.println("Update reservation cancelled success");
            
        } catch (Exception e) {
            System.err.println("Error while updating cancelled reservation:");
            e.printStackTrace();
        }
    }

    public List<Invoice> getAllInvoices(ReservationService reservationService) {
        invoices.clear();
        loadInvoicesFromDatabase(reservationService);
        return new ArrayList<>(invoices);
    }

    public List<Invoice> getUnpaidInvoices() {
        return invoices.stream()
                .filter(invoice -> !invoice.isPaid())
                .collect(java.util.stream.Collectors.toList());
    }

    public Optional<Invoice> findInvoiceById(String invoiceId) {
        return invoices.stream()
                .filter(invoice -> invoice.getId().equals(invoiceId))
                .findFirst();
    }

    public void updateInvoice(Invoice invoice) {
        int index = -1;
        for (int i = 0; i < invoices.size(); i++) {
            if (invoices.get(i).getId().equals(invoice.getId())) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            String sql = "UPDATE invoice SET isPaid = ?, reservationId = ?, subtotal = ?, tax = ?, total = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, invoice.isPaid() );
            pstmt.setString(2, invoice.getReservation().getId());
            pstmt.setDouble(3, invoice.getSubtotal());
            pstmt.setDouble(4, invoice.getTax());
            pstmt.setDouble(5, invoice.getTotal());
            pstmt.setString(6, invoice.getId());
           
            pstmt.executeUpdate();
            System.out.println("Update reservation cancelled success");
            
        } catch (Exception e) {
            System.err.println("Error while updating cancelled reservation:");
            e.printStackTrace();
        }
            invoices.set(index, invoice);
            
        }
    }
    public void generatePdf(Invoice invoice){
        try {
                    InvoicePdfGenerator generator = new InvoicePdfGenerator();
                    String pdfPath = generator.generateInvoicePdf(invoice);
                    
                    System.out.println("PDF généré avec succès : " + pdfPath);
                    
                    // Ouvrir le PDF avec l'application par défau   t
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(new File(pdfPath));
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
    }
    private void loadInvoicesFromDatabase(ReservationService reservationS) {
        String sql = "SELECT * FROM invoice";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Timestamp generationDateSql = rs.getTimestamp("generationDate");
                Invoice invoice = new Invoice(
                    rs.getString("id"),
                    reservationS.findReservationById(rs.getString("reservationId")).get(),
                    generationDateSql.toLocalDateTime(),
                    rs.getDouble("tax"),
                    rs.getDouble("total"),
                    rs.getBoolean("isPaid")
                );
                invoices.add(invoice);
                
                // Charger les réservations associées à ce client
                loadAdditionnalServices(invoice);
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des clients depuis la base de données:");
            e.printStackTrace();
        }
    }
    
    // Méthode pour charger les réservations d'un client
    private void loadAdditionnalServices(Invoice invoice) {
        String sql = "SELECT * FROM additionalService WHERE invoiceId = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, invoice.getId());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    AdditionalService additionalService = new AdditionalService(
                    rs.getString("description"),
                    rs.getDouble("price")
                );
                invoice.addAdditionalService(additionalService.getDescription(), additionalService.getPrice());
                }
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des réservations du client:");
            e.printStackTrace();
        }
    }

    public List<Invoice> getInvoicesForReservation(String reservationId) {
        return invoices.stream()
                .filter(invoice -> invoice.getReservation().getId().equals(reservationId))
                .collect(java.util.stream.Collectors.toList());
    }

    public static void craeteInvoiceTableIfNotExists() {
        // String sql3 = "drop table if exists invoice";
        // String sql4 = "drop table if exists additionalService";
        String sql1 = "CREATE TABLE IF NOT EXISTS invoice (" +
                     "id Varchar(50) PRIMARY KEY," +
                     "reservationId VARCHAR(50) NOT NULL," +
                     "generationDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                     "subtotal DOUBLE NOT NULL," +
                     "tax DOUBLE NOT NULL," +
                     "total DOUBLE NOT NULL," +
                     "isPaid BOOLEAN NOT NULL DEFAULT FALSE," +
                     "FOREIGN KEY (reservationId) REFERENCES reservation(id) ON DELETE CASCADE);";
        

        String sql = "CREATE TABLE IF NOT EXISTS additionalService (" +
                      "id INT AUTO_INCREMENT PRIMARY KEY," +
                      "invoiceId VARCHAR(50) NOT NULL," +
                      "description VARCHAR(255) NOT NULL," +
                      "price DOUBLE NOT NULL," +
                      "FOREIGN KEY (invoiceId) REFERENCES invoice(id) ON DELETE CASCADE);";
                      
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
                // stmt.executeUpdate(sql4);
                // stmt.executeUpdate(sql3);
            stmt.executeUpdate(sql1);
            System.out.println("Table 'invoice' prête !");
            stmt.executeUpdate(sql);
            System.out.println("Table 'AdditionalService' prête !");
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de la table reservation:");
            e.printStackTrace();
        }
    }
}