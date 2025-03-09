package com.hotel.view;

import com.hotel.model.Room;
import com.hotel.model.RoomCategory;
import com.hotel.service.RoomService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
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
        String[] columnNames = {"Number", "Category", "Rate/Night", "Amenities", "Status", "Actions"};
        this.tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == getColumnCount() - 1;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2) return Double.class;
                return String.class;
            }
        };
        this.roomTable = new JTable(tableModel);

        // Set table header appearance
        roomTable.getTableHeader().setBackground(new Color(51, 122, 183));
        roomTable.getTableHeader().setForeground(Color.WHITE);
        roomTable.getTableHeader().setFont(roomTable.getTableHeader().getFont().deriveFont(Font.BOLD));
        
        // Set row height to better accommodate buttons
        roomTable.setRowHeight(30);

        // Configure the Actions column with proper button handling
        // Add button editor for handling button clicks
        roomTable.getColumn("Actions").setCellEditor(new ButtonEditor(roomTable));

        roomTable.getColumn("Actions").setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            
            if (value instanceof JPanel) {
                return (JPanel) value;
            }
            
            String roomNumber = (String) tableModel.getValueAt(row, 0);
            JButton editBtn = createButton("Edit");
            JButton copyBtn = createButton("Copy ID");
            JButton deleteBtn = createButton("Delete");
            deleteBtn.setBackground(new Color(220, 53, 69)); // Red color for delete button
            
            // Set fixed size for buttons
            Dimension buttonSize = new Dimension(60, 25);
            editBtn.setPreferredSize(buttonSize);
            copyBtn.setPreferredSize(buttonSize);
            deleteBtn.setPreferredSize(buttonSize);
            
            editBtn.addActionListener(e -> {
                selectedRoomNumber = roomNumber;
                Room room = roomService.findRoomByNumber(roomNumber).orElse(null);
                if (room != null) {
                    roomNumberField.setText(room.getNumber());
                    categoryComboBox.setSelectedItem(room.getCategory());
                    rateField.setText(String.valueOf(room.getRatePerNight()));
                    amenitiesField.setText(room.getAmenities());
                }
            });
            copyBtn.addActionListener(e -> {
                StringSelection selection = new StringSelection(roomNumber);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
                JOptionPane.showMessageDialog(this, "Room number copied to clipboard!", "Copy Success", JOptionPane.INFORMATION_MESSAGE);
            });
            deleteBtn.addActionListener(e -> {
                selectedRoomNumber = roomNumber;
                deleteRoom();
            });
            
            panel.add(editBtn);
            panel.add(copyBtn);
            panel.add(deleteBtn);
            
            return panel;
        });

        // Set preferred column widths
        roomTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // Number
        roomTable.getColumnModel().getColumn(5).setPreferredWidth(150); // Actions
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
        JButton clearButton = new JButton("Clear Form");

        addButton.addActionListener(e -> addRoom());
        clearButton.addActionListener(e -> clearForm());

        buttonPanel.add(addButton);
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

    class ButtonEditor extends DefaultCellEditor {
        private JPanel panel;
        private String roomNumber;
        private int row;
        private JTable table;

        public ButtonEditor(JTable table) {
            super(new JTextField());
            this.table = table;
            this.panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            
            // Important: Set click count to 1 so it activates on first click
            setClickCountToStart(1);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.row = row;
            this.roomNumber = (String) table.getModel().getValueAt(row, 0);
            
            // Clear panel and recreate buttons
            panel.removeAll();
            panel.setBackground(table.getSelectionBackground());
            
            // Create buttons with the same styling as in your renderer
            JButton editBtn = createButton("Edit");
            JButton copyBtn = createButton("Copy ID");
            JButton deleteBtn = createButton("Delete");
            deleteBtn.setBackground(new Color(220, 53, 69)); // Red color for delete button
            
            // Set fixed size for buttons
            Dimension buttonSize = new Dimension(60, 25);
            editBtn.setPreferredSize(buttonSize);
            copyBtn.setPreferredSize(buttonSize);
            deleteBtn.setPreferredSize(buttonSize);
            
            // Add action listeners that call your existing methods
            RoomPanel roomPanel = (RoomPanel) SwingUtilities.getAncestorOfClass(RoomPanel.class, table);
            if (roomPanel != null) {
                editBtn.addActionListener(e -> {
                    stopCellEditing();
                    roomPanel.selectedRoomNumber = roomNumber;
                    Room room = roomPanel.roomService.findRoomByNumber(roomNumber).orElse(null);
                    if (room != null) {
                        roomPanel.roomNumberField.setText(room.getNumber());
                        roomPanel.categoryComboBox.setSelectedItem(room.getCategory());
                        roomPanel.rateField.setText(String.valueOf(room.getRatePerNight()));
                        roomPanel.amenitiesField.setText(room.getAmenities());
                    }
                });
                copyBtn.addActionListener(e -> {
                    stopCellEditing();
                    StringSelection selection = new StringSelection(roomNumber);
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
                    JOptionPane.showMessageDialog(roomPanel, "Room number copied to clipboard!", "Copy Success", JOptionPane.INFORMATION_MESSAGE);
                });
                deleteBtn.addActionListener(e -> {
                    stopCellEditing();
                    roomPanel.selectedRoomNumber = roomNumber;
                    roomPanel.deleteRoom();
                });
            }
            
            panel.add(editBtn);
            panel.add(copyBtn);
            panel.add(deleteBtn);
            
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return panel;
        }
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(button.getFont().deriveFont(11f));
        button.setMargin(new Insets(1, 3, 1, 3));
        button.setFocusPainted(false);
        button.setBackground(new Color(51, 122, 183));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        return button;
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
                room.isOccupied() ? "Occupied" : "Available",
                new JPanel() // Placeholder for actions column
            };
            tableModel.addRow(row);
        }
    }
}