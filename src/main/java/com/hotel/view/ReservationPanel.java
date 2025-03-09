package com.hotel.view;

import com.hotel.model.Reservation;
import com.hotel.service.ReservationService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ReservationPanel extends JPanel {
    private final ReservationService reservationService;
    private final JTable reservationTable;
    private final DefaultTableModel tableModel;
    private final JTextField clientIdField = new JTextField(20);
    private final JTextField roomNumberField = new JTextField(10);
    private final JTextField checkInField = new JTextField(10);
    private final JTextField checkOutField = new JTextField(10);
    private String selectedReservationId;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ReservationPanel(ReservationService reservationService) {
        if (reservationService == null) {
            throw new IllegalArgumentException("ReservationService cannot be null");
        }
        this.reservationService = reservationService;
        this.setLayout(new BorderLayout(10, 10));

        // Initialize table model
        String[] columnNames = {"ID", "Client", "Room", "Check-In", "Check-Out", "Total Price", "Status"};
        this.tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Initialize table
        this.reservationTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(reservationTable);
        add(scrollPane, BorderLayout.CENTER);

        // Create form panel
        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.NORTH);

        // Add selection listener
        reservationTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && reservationTable.getSelectedRow() != -1) {
                selectedReservationId = (String) tableModel.getValueAt(reservationTable.getSelectedRow(), 0);
            }
        });

        // Initial table load
        refreshTable();
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Add form components
        addFormRow(formPanel, "Client ID:", clientIdField, gbc, 0);
        addFormRow(formPanel, "Room Number:", roomNumberField, gbc, 1);
        addFormRow(formPanel, "Check-In Date (yyyy-MM-dd):", checkInField, gbc, 2);
        addFormRow(formPanel, "Check-Out Date (yyyy-MM-dd):", checkOutField, gbc, 3);

        // Add buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton createButton = new JButton("Create Reservation");
        JButton cancelButton = new JButton("Cancel Reservation");
        JButton clearButton = new JButton("Clear Form");

        createButton.addActionListener(e -> createReservation());
        cancelButton.addActionListener(e -> cancelReservation());
        clearButton.addActionListener(e -> clearForm());

        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(clearButton);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        return formPanel;
    }

    private void addFormRow(JPanel panel, String label, JTextField field, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(field, gbc);
    }

    private void createReservation() {
        if (!validateInputFields()) {
            return;
        }

        try {
            String clientId = clientIdField.getText().trim();
            String roomNumber = roomNumberField.getText().trim();
            LocalDate checkIn = LocalDate.parse(checkInField.getText().trim(), DATE_FORMATTER);
            LocalDate checkOut = LocalDate.parse(checkOutField.getText().trim(), DATE_FORMATTER);

            if (checkOut.isBefore(checkIn)) {
                JOptionPane.showMessageDialog(this, "Check-out date cannot be before check-in date");
                return;
            }

            if (checkIn.isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(this, "Check-in date cannot be in the past");
                return;
            }

            try {
                reservationService.createReservation(clientId, roomNumber, checkIn, checkOut);
                clearForm();
                refreshTable();
                JOptionPane.showMessageDialog(this, "Reservation created successfully");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error creating reservation: " + e.getMessage());
            }
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid dates in yyyy-MM-dd format");
        }
    }

    private boolean validateInputFields() {
        if (clientIdField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Client ID");
            return false;
        }

        if (roomNumberField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Room Number");
            return false;
        }

        if (checkInField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Check-In Date");
            return false;
        }

        if (checkOutField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Check-Out Date");
            return false;
        }

        return true;
    }

    private void cancelReservation() {
        if (selectedReservationId == null) {
            JOptionPane.showMessageDialog(this, "Please select a reservation to cancel");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel this reservation?",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            reservationService.cancelReservation(selectedReservationId);
            clearForm();
            refreshTable();
        }
    }

    private void clearForm() {
        clientIdField.setText("");
        roomNumberField.setText("");
        checkInField.setText("");
        checkOutField.setText("");
        selectedReservationId = null;
        reservationTable.clearSelection();
    }

    private void refreshTable() {
        try {
            tableModel.setRowCount(0);
            List<Reservation> reservations = reservationService.getAllReservations();
            if (reservations != null) {
                for (Reservation reservation : reservations) {
                    if (reservation != null) {
                        Object[] row = {
                            reservation.getId(),
                            reservation.getClient() != null ? reservation.getClient().toString() : "N/A",
                            reservation.getRoom() != null ? reservation.getRoom().toString() : "N/A",
                            reservation.getCheckInDate().format(DATE_FORMATTER),
                            reservation.getCheckOutDate().format(DATE_FORMATTER),
                            String.format("%.2f", reservation.getTotalPrice()),
                            reservation.isCancelled() ? "Cancelled" : "Active"
                        };
                        tableModel.addRow(row);
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error refreshing reservation table: " + e.getMessage());
        }
    }
}