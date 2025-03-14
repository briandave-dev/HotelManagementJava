package com.hotel.view;

import com.hotel.model.Reservation;
import com.hotel.service.ReservationService;
import com.hotel.service.InvoiceService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

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
    // Define consistent colors
    private final Color PRIMARY_BLUE = new Color(51, 122, 183);
    private final Color DANGER_RED = new Color(220, 53, 69);
    private final Color SUCCESS_GREEN = new Color(40, 167, 69);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ReservationPanel(ReservationService reservationService, InvoiceService invoiceService) {
        if (reservationService == null) {
            throw new IllegalArgumentException("ReservationService cannot be null");
        }
        this.reservationService = reservationService;
        this.invoiceService = invoiceService;
        this.setLayout(new BorderLayout(10, 10));
        this.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Add title
        JLabel titleLabel = new JLabel("Reservation Management", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Initialize table model
        String[] columnNames = { "ID", "Client", "Room", "Check-In", "Check-Out", "Total Price", "Status", "Actions" };
        this.tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Make only the Actions column editable
                return column == 7; // 7 is the index of the Actions column
            }
        };

        // Initialize table
        this.reservationTable = new JTable(tableModel);

        // Set table header appearance
        reservationTable.getTableHeader().setBackground(PRIMARY_BLUE);
        // reservationTable.getTableHeader().setForeground(Color.WHITE);
        reservationTable.getTableHeader().setFont(reservationTable.getTableHeader().getFont().deriveFont(Font.BOLD));

        // Remove hover effect from rows
        reservationTable.setSelectionBackground(reservationTable.getBackground());
        reservationTable.setSelectionForeground(reservationTable.getForeground());

        // Set row height and selection mode
        reservationTable.setRowHeight(35);
        reservationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Set up the custom renderer for the Actions column
        reservationTable.getColumn("Actions").setCellRenderer(new ButtonRenderer());

        // Set up the custom editor for the Actions column
        reservationTable.getColumn("Actions").setCellEditor(new ButtonEditor(this));

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
        // Create form panel with rounded borders and styling
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        formPanel.setBackground(new Color(245, 245, 245));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Add form fields with improved styling
        addFormField(formPanel, "Client ID:", clientIdField, gbc, 0);
        addFormField(formPanel, "Room Number:", roomNumberField, gbc, 1);
        addFormField(formPanel, "Check-In Date (yyyy-MM-dd):", checkInField, gbc, 2);
        addFormField(formPanel, "Check-Out Date (yyyy-MM-dd):", checkOutField, gbc, 3);

        // Add buttons with improved styling
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);

        JButton createButton = createStyledButton("Create Reservation", new Color(40, 167, 69));
        JButton clearButton = createStyledButton("Clear Form", new Color(108, 117, 125));

        createButton.addActionListener(e -> createReservation());
        clearButton.addActionListener(e -> clearForm());

        buttonPanel.add(createButton);
        buttonPanel.add(clearButton);

        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(buttonPanel, gbc);

        // Create a wrapper panel to center the form
        JPanel wrapperPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrapperPanel.add(formPanel);
        wrapperPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Add title
        JLabel titleLabel = new JLabel("Reservation Management", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Create a main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(headerPanel, BorderLayout.NORTH);
        contentPanel.add(wrapperPanel, BorderLayout.CENTER);

        return contentPanel;
    }

    // Helper method to add form fields with consistent styling
    private void addFormField(JPanel panel, String labelText, JComponent field, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;

        if (field instanceof JTextField) {
            ((JTextField) field).setPreferredSize(new Dimension(field.getPreferredSize().width, 30));
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    BorderFactory.createEmptyBorder(2, 5, 2, 5)));
        }
        panel.add(field, gbc);
    }

    // Create styled button with consistent appearance
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(120, 35));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
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
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField checkInField = new JTextField(reservation.getCheckInDate().format(DATE_FORMATTER), 15);
        JTextField checkOutField = new JTextField(reservation.getCheckOutDate().format(DATE_FORMATTER), 15);

        // Use addFormField instead of addFormRow for consistency
        addFormField(formPanel, "Check-In Date:", checkInField, gbc, 0);
        addFormField(formPanel, "Check-Out Date:", checkOutField, gbc, 1);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        // Apply consistent styling to buttons
        saveButton.setBackground(new Color(51, 122, 183));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);

        cancelButton.setBackground(new Color(108, 117, 125));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);

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
                if (!reservationService.isRoomAvailable(reservation.getRoom().getNumber(), checkIn, checkOut,
                        reservation.getId())) {
                    JOptionPane.showMessageDialog(dialog, "Room is not available for the selected dates");
                    return;
                }

                // Update reservation dates
                try {
                    reservationService.updateReservationDates(reservation.getId(), checkIn, checkOut);
                    refreshTable();
                    dialog.dispose();
                    showToast("Reservation updated successfully");
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

    public void refreshTable() {
        tableModel.setRowCount(0);
        List<Reservation> reservations = reservationService.getAllReservations();
        for (Reservation reservation : reservations) {
            Object[] row = {
                    reservation.getId(),
                    reservation.getClient().getName(),
                    reservation.getRoom().getNumber(),
                    reservation.getCheckInDate().format(DATE_FORMATTER),
                    reservation.getCheckOutDate().format(DATE_FORMATTER),
                    String.format("$%.2f", reservation.getTotalPrice()),
                    reservation.isCancelled() ? "Cancelled" : "Active",
                    "ACTIONS" // Placeholder that will be replaced by the renderer
            };
            tableModel.addRow(row);
        }
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        // button.setForeground(Color.WHITE);
        if (text.equals("Cancel")) {
            button.setBackground(new Color(220, 53, 69)); // Red color for cancel button
        } else {
            button.setBackground(new Color(51, 122, 183)); // Blue color for other buttons
        }
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    // Custom renderer for the button column
    class ButtonRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            panel.setBackground(table.getBackground()); // Always use default background

            JButton cancelBtn = createStyledButton("Cancel", DANGER_RED);
            cancelBtn.setBackground(new Color(220, 53, 69)); // Red for cancel

            // Set fixed size for button
            Dimension buttonSize = new Dimension(80, 25);
            cancelBtn.setPreferredSize(buttonSize);

            panel.add(cancelBtn);
            return panel;
        }
    }

    // Custom editor for the button column
    class ButtonEditor extends DefaultCellEditor {
        private JPanel panel;
        private JButton cancelButton;
        private String reservationId;
        private ReservationPanel reservationPanel;

        public ButtonEditor(ReservationPanel reservationPanel) {
            super(new JTextField());
            this.reservationPanel = reservationPanel;

            panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            cancelButton = createButton("Cancel");

            // Set fixed size for button
            Dimension buttonSize = new Dimension(80, 25);
            cancelButton.setPreferredSize(buttonSize);

            cancelButton.addActionListener(e -> {
                fireEditingStopped();
                reservationPanel.cancelReservation(reservationId);
            });

            panel.add(cancelButton);

            // Important: Set click count to 1 for immediate activation
            setClickCountToStart(1);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            // Get reservation ID from the first column
            reservationId = (String) tableModel.getValueAt(row, 0);

            // Get the reservation status
            String status = (String) tableModel.getValueAt(row, 6);

            // Disable the button if the reservation is already cancelled
            cancelButton.setEnabled(!status.equals("Cancelled"));

            panel.setBackground(table.getSelectionBackground());

            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "ACTIONS"; // Return a placeholder value
        }
    }

    public void cancelReservation(String reservationId) {
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

    // Add a toast notification method
    private void showToast(String message) {
        JDialog toastDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this));
        toastDialog.setUndecorated(true);
        toastDialog.setLayout(new BorderLayout());

        JPanel toastPanel = new JPanel();
        toastPanel.setBackground(new Color(51, 51, 51, 230));
        toastPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        toastPanel.setLayout(new BorderLayout());

        JLabel toastLabel = new JLabel(message);
        toastLabel.setForeground(Color.WHITE);
        toastLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        toastPanel.add(toastLabel, BorderLayout.CENTER);

        toastDialog.add(toastPanel);
        toastDialog.pack();

        // Center toast relative to parent window
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        if (owner != null) {
            int x = owner.getX() + (owner.getWidth() - toastDialog.getWidth()) / 2;
            int y = owner.getY() + owner.getHeight() - toastDialog.getHeight() - 50;
            toastDialog.setLocation(x, y);
        }

        toastDialog.setVisible(true);

        // Auto-hide toast after 2 seconds
        new Timer(2000, e -> {
            toastDialog.dispose();
        }).start();
    }
}