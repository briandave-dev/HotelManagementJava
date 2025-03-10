package com.hotel.view;

import com.hotel.model.Reservation;
import com.hotel.service.ReservationService;
import com.hotel.service.InvoiceService;
import com.hotel.util.PDFGenerator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;
import java.io.File;
import java.util.Optional;

public class ReservationPanel extends JPanel {
    private final ReservationService reservationService;
    private final InvoiceService invoiceService;
    private final JTable reservationTable;
    private final DefaultTableModel tableModel;
    private final JTextField clientIdField = new JTextField(20);
    private final JTextField roomNumberField = new JTextField(10);
    private final JTextField checkInField = new JTextField(10);
    private final JTextField checkOutField = new JTextField(10);
    private String selectedReservationId;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ReservationPanel(ReservationService reservationService, InvoiceService invoiceService) {
        if (reservationService == null) {
            throw new IllegalArgumentException("ReservationService cannot be null");
        }
        this.reservationService = reservationService;
        this.invoiceService = invoiceService;
        this.setLayout(new BorderLayout(10, 10));

        // Initialize table model
        String[] columnNames = {"ID", "Client", "Room", "Check-In", "Check-Out", "Total Price", "Status", "Actions"};
        this.tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Initialize table
        this.reservationTable = new JTable(tableModel);
        
        // Set table header appearance
        reservationTable.getTableHeader().setBackground(new Color(51, 122, 183));
        reservationTable.getTableHeader().setForeground(Color.WHITE);
        reservationTable.getTableHeader().setFont(reservationTable.getTableHeader().getFont().deriveFont(Font.BOLD));
        
        // Set row height and selection mode
        reservationTable.setRowHeight(35);
        reservationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        this.reservationTable.getColumn("Actions").setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
            if (value instanceof JPanel) {
                JPanel panel = (JPanel) value;
                panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                return panel;
            }

            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            
            JButton cancelBtn = createButton("Cancel");
            cancelBtn.setBackground(new Color(220, 53, 69)); // Red color for cancel button
            
            // Set fixed size for button
            Dimension buttonSize = new Dimension(80, 25);
            cancelBtn.setPreferredSize(buttonSize);
            
            cancelBtn.addActionListener(e -> cancelReservation((String) tableModel.getValueAt(row, 0)));
            
            panel.add(cancelBtn);
            return panel;
        });
        
        // Remove the cell editor since we don't need to edit the buttons
        // Add button editor for handling button clicks
        this.reservationTable.getColumn("Actions").setCellEditor(new ButtonEditor(reservationTable));
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
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        // Center the form components
        JPanel centerPanel = new JPanel(new GridBagLayout());
        
        // Add form components
        addFormRow(centerPanel, "Client ID:", clientIdField, gbc, 0);
        addFormRow(centerPanel, "Room Number:", roomNumberField, gbc, 1);
        addFormRow(centerPanel, "Check-In Date (yyyy-MM-dd):", checkInField, gbc, 2);
        addFormRow(centerPanel, "Check-Out Date (yyyy-MM-dd):", checkOutField, gbc, 3);

        // Add buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton createButton = new JButton("Create Reservation");
        JButton clearButton = new JButton("Clear Form");

        // Style the buttons
        createButton.setBackground(new Color(51, 122, 183));
        createButton.setForeground(Color.WHITE);
        clearButton.setBackground(new Color(108, 117, 125));
        clearButton.setForeground(Color.WHITE);
        
        createButton.setFocusPainted(false);
        clearButton.setFocusPainted(false);

        createButton.addActionListener(e -> createReservation());
        clearButton.addActionListener(e -> clearForm());

        buttonPanel.add(createButton);
        buttonPanel.add(clearButton);

        // Add centerPanel to formPanel with proper constraints
        GridBagConstraints centerConstraints = new GridBagConstraints();
        centerConstraints.gridx = 0;
        centerConstraints.gridy = 0;
        centerConstraints.weightx = 1.0;
        centerConstraints.weighty = 1.0;
        centerConstraints.fill = GridBagConstraints.NONE;
        centerConstraints.anchor = GridBagConstraints.CENTER;
        formPanel.add(centerPanel, centerConstraints);

        // Add button panel
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        centerPanel.add(buttonPanel, gbc);

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

            // Check if room is available for the selected dates
            if (!reservationService.isRoomAvailable(roomNumber, checkIn, checkOut)) {
                JOptionPane.showMessageDialog(this, "Room is not available for the selected dates");
                return;
            }

            Reservation reservation = reservationService.createReservation(clientId, roomNumber, checkIn, checkOut);
            if (reservation == null) {
                throw new Exception("Failed to create reservation");
            }

            // Generate invoice for the new reservation
            invoiceService.generateInvoice(reservation);
            clearForm();
            refreshTable();
            JOptionPane.showMessageDialog(this, "Reservation created successfully and invoice generated");
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid dates in yyyy-MM-dd format");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error creating reservation: " + e.getMessage());
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

    private void showEditDialog(Reservation reservation) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Reservation", true);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField checkInField = new JTextField(reservation.getCheckInDate().format(DATE_FORMATTER), 15);
        JTextField checkOutField = new JTextField(reservation.getCheckOutDate().format(DATE_FORMATTER), 15);

        addFormRow(formPanel, "Check-In Date:", checkInField, gbc, 0);
        addFormRow(formPanel, "Check-Out Date:", checkOutField, gbc, 1);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            try {
                LocalDate checkIn = LocalDate.parse(checkInField.getText().trim(), DATE_FORMATTER);
                LocalDate checkOut = LocalDate.parse(checkOutField.getText().trim(), DATE_FORMATTER);

                if (checkOut.isBefore(checkIn)) {
                    JOptionPane.showMessageDialog(dialog, "Check-out date cannot be before check-in date");
                    return;
                }

                if (checkIn.isBefore(LocalDate.now())) {
                    JOptionPane.showMessageDialog(dialog, "Check-in date cannot be in the past");
                    return;
                }

                // Check if room is available for the new dates (excluding current reservation)
                if (!reservationService.isRoomAvailable(reservation.getRoom().getNumber(), checkIn, checkOut, reservation.getId())) {
                    JOptionPane.showMessageDialog(dialog, "Room is not available for the selected dates");
                    return;
                }

                // Update reservation dates
                try {
                    reservationService.updateReservationDates(reservation.getId(), checkIn, checkOut);
                    refreshTable();
                    dialog.dispose();
                    JOptionPane.showMessageDialog(this, "Reservation updated successfully");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Error updating reservation: " + ex.getMessage());
                }
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid dates in yyyy-MM-dd format");
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
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
        tableModel.setRowCount(0);
        List<Reservation> reservations = reservationService.getAllReservations();
        for (Reservation reservation : reservations) {
            JPanel actionPanel = createActionPanel(reservation);
            Object[] row = {
                reservation.getId(),
                reservation.getClient().getName(),
                reservation.getRoom().getNumber(),
                reservation.getCheckInDate().format(DATE_FORMATTER),
                reservation.getCheckOutDate().format(DATE_FORMATTER),
                String.format("$%.2f", reservation.getTotalPrice()),
                reservation.isCancelled() ? "Cancelled" : "Active",
                actionPanel
            };
            tableModel.addRow(row);
        }
        
        // Set the row height to accommodate the buttons
        reservationTable.setRowHeight(35);
        
        // Ensure the Actions column uses the custom renderer
        reservationTable.getColumn("Actions").setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof JPanel) {
                    JPanel panel = (JPanel) value;
                    panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                    return panel;
                }
                return new JLabel();
            }
        });
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setForeground(Color.WHITE);
        if (text.equals("Cancel")) {
            button.setBackground(new Color(220, 53, 69)); // Red color for cancel button
        } else {
            button.setBackground(new Color(51, 122, 183)); // Blue color for other buttons
        }
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        return button;
    }

    class ButtonEditor extends DefaultCellEditor {
        private JPanel panel;
        private String reservationId;
        private JTable table;

        public ButtonEditor(JTable table) {
            super(new JTextField());
            this.table = table;
            this.panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            setClickCountToStart(1);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.reservationId = (String) table.getModel().getValueAt(row, 0);
            
            panel.removeAll();
            panel.setBackground(table.getSelectionBackground());
            
            JButton cancelButton = createButton("Cancel");
            cancelButton.setEnabled(!((String)table.getModel().getValueAt(row, 6)).equals("Cancelled"));
            
            Dimension buttonSize = new Dimension(80, 25);
            cancelButton.setPreferredSize(buttonSize);
            
            ReservationPanel reservationPanel = (ReservationPanel) SwingUtilities.getAncestorOfClass(ReservationPanel.class, table);
            if (reservationPanel != null) {
                cancelButton.addActionListener(e -> {
                    stopCellEditing();
                    int confirm = JOptionPane.showConfirmDialog(reservationPanel,
                        "Are you sure you want to cancel this reservation?",
                        "Confirm Cancellation",
                        JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        reservationPanel.cancelReservation(reservationId);
                    }
                });
            }
            
            panel.add(cancelButton);
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return panel;
        }
    }

    private JPanel createActionPanel(Reservation reservation) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        panel.setOpaque(true);
        
        JButton cancelButton = createButton("Cancel");
        cancelButton.setEnabled(!reservation.isCancelled());
        
        // Set fixed size for button
        Dimension buttonSize = new Dimension(80, 25);
        cancelButton.setPreferredSize(buttonSize);
        
        panel.add(cancelButton);
        return panel;
    }


private void cancelReservation(String reservationId) {
    if (reservationId == null || reservationId.isEmpty()) {
        JOptionPane.showMessageDialog(this,
            "Please select a reservation to cancel",
            "Error",
            JOptionPane.ERROR_MESSAGE);
        return;
    }

    int confirm = JOptionPane.showConfirmDialog(this,
        "Are you sure you want to cancel this reservation?",
        "Confirm Cancellation",
        JOptionPane.YES_NO_OPTION);

    if (confirm == JOptionPane.YES_OPTION) {
        try {
            reservationService.cancelReservation(reservationId);
            refreshTable(); // Refresh the table to show updated status
            JOptionPane.showMessageDialog(this,
                "Reservation cancelled successfully",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this,
                "Error cancelling reservation: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
}
