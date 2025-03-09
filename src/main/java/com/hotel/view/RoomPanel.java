package com.hotel.view;

import com.hotel.model.Room;
import com.hotel.model.RoomCategory;
import com.hotel.service.RoomService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class RoomPanel extends JPanel {
    private final RoomService roomService;
    private final JTable roomTable;
    private final DefaultTableModel tableModel;
    private final JTextField roomNumberField;
    private final JComboBox<RoomCategory> categoryComboBox;
    private final JTextField rateField;
    private final JTextField amenitiesField;
    private String selectedRoomNumber;

    public RoomPanel(RoomService roomService) {
        this.roomService = roomService;
        this.setLayout(new BorderLayout(10, 10));

        // Initialize form components
        this.roomNumberField = new JTextField(10);
        this.categoryComboBox = new JComboBox<>(RoomCategory.values());
        this.rateField = new JTextField(10);
        this.amenitiesField = new JTextField(20);

        // Create table model and table
        String[] columnNames = {"Number", "Category", "Rate/Night", "Amenities", "Status"};
        this.tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2) return Double.class;
                return String.class;
            }
        };
        this.roomTable = new JTable(tableModel);
        roomTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && roomTable.getSelectedRow() != -1) {
                selectedRoomNumber = (String) roomTable.getValueAt(roomTable.getSelectedRow(), 0);
                Room room = roomService.findRoomByNumber(selectedRoomNumber).orElse(null);
                if (room != null) {
                    roomNumberField.setText(room.getNumber());
                    categoryComboBox.setSelectedItem(room.getCategory());
                    rateField.setText(String.valueOf(room.getRatePerNight()));
                    amenitiesField.setText(room.getAmenities());
                }
            }
        });

        // Create and add form panel
        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.NORTH);

        // Add table with scroll pane
        JScrollPane scrollPane = new JScrollPane(roomTable);
        add(scrollPane, BorderLayout.CENTER);

        // Initial table load
        refreshTable();
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Add form components
        addFormRow(formPanel, "Room Number:", roomNumberField, gbc, 0);
        addFormRow(formPanel, "Category:", categoryComboBox, gbc, 1);
        addFormRow(formPanel, "Rate per Night:", rateField, gbc, 2);
        addFormRow(formPanel, "Amenities:", amenitiesField, gbc, 3);

        // Add buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton addButton = new JButton("Add Room");
        JButton updateButton = new JButton("Update Room");
        JButton deleteButton = new JButton("Delete Room");
        JButton clearButton = new JButton("Clear Form");

        addButton.addActionListener(e -> addRoom());
        updateButton.addActionListener(e -> updateRoom());
        deleteButton.addActionListener(e -> deleteRoom());
        clearButton.addActionListener(e -> clearForm());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        return formPanel;
    }

    private void addFormRow(JPanel panel, String label, JComponent field, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(field, gbc);
    }

    private void addRoom() {
        if (roomNumberField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a room number");
            return;
        }

        try {
            double rate = Double.parseDouble(rateField.getText());
            if (rate <= 0) {
                JOptionPane.showMessageDialog(this, "Rate must be greater than 0");
                return;
            }

            roomService.addRoom(
                roomNumberField.getText().trim(),
                (RoomCategory) categoryComboBox.getSelectedItem(),
                rate,
                amenitiesField.getText().trim()
            );
            clearForm();
            refreshTable();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid rate");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding room: " + e.getMessage());
        }
    }

    private void updateRoom() {
        if (selectedRoomNumber == null) {
            JOptionPane.showMessageDialog(this, "Please select a room to update");
            return;
        }

        try {
            double rate = Double.parseDouble(rateField.getText());
            if (rate <= 0) {
                JOptionPane.showMessageDialog(this, "Rate must be greater than 0");
                return;
            }

            roomService.updateRoom(
                selectedRoomNumber,
                (RoomCategory) categoryComboBox.getSelectedItem(),
                rate,
                amenitiesField.getText().trim()
            );
            clearForm();
            refreshTable();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid rate");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating room: " + e.getMessage());
        }
    }

    private void deleteRoom() {
        if (selectedRoomNumber == null) {
            JOptionPane.showMessageDialog(this, "Please select a room to delete");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this room?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                roomService.deleteRoom(selectedRoomNumber);
                clearForm();
                refreshTable();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error deleting room: " + e.getMessage());
            }
        }
    }

    private void clearForm() {
        roomNumberField.setText("");
        categoryComboBox.setSelectedIndex(0);
        rateField.setText("");
        amenitiesField.setText("");
        selectedRoomNumber = null;
        roomTable.clearSelection();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Room> rooms = roomService.getAllRooms();
        for (Room room : rooms) {
            Object[] row = {
                room.getNumber(),
                room.getCategory(),
                room.getRatePerNight(),
                room.getAmenities(),
                room.isOccupied() ? "Occupied" : "Available"
            };
            tableModel.addRow(row);
        }
    }
}