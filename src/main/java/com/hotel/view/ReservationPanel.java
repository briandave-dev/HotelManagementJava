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
        
        this.reservationTable.getColumn("Actions").setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof JPanel) {
                    JPanel panel = (JPanel) value;
                    panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                    return panel;
                }
                return new JPanel();
            }
        });
        
        // Remove the cell editor since we don't need to edit the buttons
        this.reservationTable.getColumn("Actions").setCellEditor(null);
        this.reservationTable.getColumn("Actions").setCellEditor(new DefaultCellEditor(new JTextField()) {
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value,
                    boolean isSelected, int row, int column) {
                return (JPanel) value;
            }
        });
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
        JButton clearButton = new JButton("Clear Form");

        createButton.addActionListener(e -> createReservation());
        clearButton.addActionListener(e -> clearForm());

        buttonPanel.add(createButton);
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
            try {
                invoiceService.generateInvoice(reservation);
                clearForm();
                refreshTable();
                JOptionPane.showMessageDialog(this, "Reservation created successfully");
            } catch (Exception e) {
                // If invoice generation fails, cancel the reservation
                reservationService.cancelReservation(reservation.getId());
                throw new Exception("Failed to generate invoice: " + e.getMessage());
            }
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
            Object[] row = {
                reservation.getId(),
                reservation.getClient().getName(),
                reservation.getRoom().getNumber(),
                reservation.getCheckInDate().format(DATE_FORMATTER),
                reservation.getCheckOutDate().format(DATE_FORMATTER),
                String.format("$%.2f", reservation.getTotalPrice()),
                reservation.isCancelled() ? "Cancelled" : "Active",
                createActionPanel(reservation)
            };
            tableModel.addRow(row);
        }
    }

//     private JPanel createActionPanel(Reservation reservation) {
//         JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
//         panel.setOpaque(true);
        
//         JButton editButton = createStyledButton("Edit");
//         JButton cancelButton = createStyledButton("Cancel");
//         JButton viewInvoiceButton = createStyledButton("View Invoice");
//         JButton copyClientIdButton = createStyledButton("Copy Client ID");
//         JButton copyRoomIdButton = createStyledButton("Copy Room ID");

//         // Disable buttons if reservation is cancelled
//         editButton.setEnabled(!reservation.isCancelled());
//         cancelButton.setEnabled(!reservation.isCancelled());

//         editButton.addActionListener(e -> showEditDialog(reservation));
        
//         cancelButton.addActionListener(e -> {
//             if (!reservation.isCancelled()) {
//                 int confirm = JOptionPane.showConfirmDialog(this,
//                         "Are you sure you want to cancel this reservation?",
//                         "Confirm Cancellation",
//                         JOptionPane.YES_NO_OPTION);
//                 if (confirm == JOptionPane.YES_OPTION) {
//                     try {
//                         reservationService.cancelReservation(reservation.getId());
//                         refreshTable();
//                     } catch (Exception ex) {
//                         JOptionPane.showMessageDialog(this, "Error cancelling reservation: " + ex.getMessage());
//                     }
//                 }
//             }
//         });

//         viewInvoiceButton.addActionListener(e -> {
//             try {
//                 invoiceService.getInvoicesForReservation(reservation.getId())
//                         .stream()
//                         .findFirst()
//                         .ifPresentOrElse(invoice -> {
//                             try {
//                                 String pdfPath = PDFGenerator.generateInvoicePDF(invoice);
//                                 if (pdfPath == null || pdfPath.isEmpty()) {
//                                     throw new Exception("Failed to generate PDF");
//                                 }
//                                 File pdfFile = new File(pdfPath);
//                                 if (!pdfFile.exists()) {
//                                     throw new Exception("PDF file not found");
//                                 }
//                                 Desktop.getDesktop().open(pdfFile);
//                             } catch (Exception ex) {
//                                 JOptionPane.showMessageDialog(this, "Error opening PDF: " + ex.getMessage());
//                             }
//                         }, () -> JOptionPane.showMessageDialog(this, "No invoice found for this reservation"));
//             } catch (Exception ex) {
//                 JOptionPane.showMessageDialog(this, "Error processing invoice: " + ex.getMessage());
//             }
//         });

//         copyClientIdButton.addActionListener(e -> {
//             try {
//                 StringSelection selection = new StringSelection(reservation.getClient().getId());
//                 Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
//             } catch (Exception ex) {
//                 JOptionPane.showMessageDialog(this, "Error copying client ID: " + ex.getMessage());
//             }
//         });

//         copyRoomIdButton.addActionListener(e -> {
//             try {
//                 StringSelection selection = new StringSelection(reservation.getRoom().getNumber());
//                 Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
//             } catch (Exception ex) {
//                 JOptionPane.showMessageDialog(this, "Error copying room ID: " + ex.getMessage());
//             }
//         });

//         panel.add(editButton);
//         panel.add(cancelButton);
//         panel.add(viewInvoiceButton);
//         panel.add(copyClientIdButton);
//         panel.add(copyRoomIdButton);

//         return panel;
//     }
// }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(button.getFont().deriveFont(11f));
        button.setMargin(new Insets(1, 3, 1, 3));
        button.setFocusPainted(false);
        button.setBackground(new Color(51, 122, 183));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        button.setPreferredSize(new Dimension(90, 25));
        return button;
    }

    private JPanel createActionPanel(Reservation reservation) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        panel.setOpaque(true);
        
        JButton editButton = new JButton("Edit");
        JButton cancelButton = new JButton("Cancel");
        JButton viewInvoiceButton = new JButton("View Invoice");
        JButton copyClientIdButton = new JButton("Copy Client ID");
        JButton copyRoomIdButton = new JButton("Copy Room ID");

        // Disable buttons if reservation is cancelled
        editButton.setEnabled(!reservation.isCancelled());
        cancelButton.setEnabled(!reservation.isCancelled());

        editButton.addActionListener(e -> showEditDialog(reservation));
        
        cancelButton.addActionListener(e -> {
            if (!reservation.isCancelled()) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to cancel this reservation?",
                        "Confirm Cancellation",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        reservationService.cancelReservation(reservation.getId());
                        refreshTable();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Error cancelling reservation: " + ex.getMessage());
                    }
                }
            }
        });

        viewInvoiceButton.addActionListener(e -> {
            try {
                invoiceService.getInvoicesForReservation(reservation.getId())
                        .stream()
                        .findFirst()
                        .ifPresentOrElse(invoice -> {
                            try {
                                String pdfPath = PDFGenerator.generateInvoicePDF(invoice);
                                if (pdfPath == null || pdfPath.isEmpty()) {
                                    throw new Exception("Failed to generate PDF");
                                }
                                File pdfFile = new File(pdfPath);
                                if (!pdfFile.exists()) {
                                    throw new Exception("PDF file not found");
                                }
                                Desktop.getDesktop().open(pdfFile);
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(this, "Error opening PDF: " + ex.getMessage());
                            }
                        }, () -> JOptionPane.showMessageDialog(this, "No invoice found for this reservation"));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error processing invoice: " + ex.getMessage());
            }
        });

        copyClientIdButton.addActionListener(e -> {
            try {
                StringSelection selection = new StringSelection(reservation.getClient().getId());
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error copying client ID: " + ex.getMessage());
            }
        });

        copyRoomIdButton.addActionListener(e -> {
            try {
                StringSelection selection = new StringSelection(reservation.getRoom().getNumber());
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error copying room ID: " + ex.getMessage());
            }
        });

        panel.add(editButton);
        panel.add(cancelButton);
        panel.add(viewInvoiceButton);
        panel.add(copyClientIdButton);
        panel.add(copyRoomIdButton);

        return panel;
    }
}