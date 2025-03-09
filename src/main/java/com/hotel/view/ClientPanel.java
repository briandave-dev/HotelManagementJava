package com.hotel.view;

import com.hotel.model.Client;
import com.hotel.service.ClientService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class ClientPanel extends JPanel {
    private final ClientService clientService;
    private final JTable clientTable;
    private final DefaultTableModel tableModel;
    private final JTextField firstNameField;
    private final JTextField lastNameField;
    private final JTextField addressField;
    private final JTextField phoneField;
    private final JTextField emailField;
    private String selectedClientId;

    // Initialize components
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Add form components
        addFormRow(formPanel, "First Name:", firstNameField, gbc, 0);
        addFormRow(formPanel, "Last Name:", lastNameField, gbc, 1);
        addFormRow(formPanel, "Address:", addressField, gbc, 2);
        addFormRow(formPanel, "Phone:", phoneField, gbc, 3);
        addFormRow(formPanel, "Email:", emailField, gbc, 4);

        // Add buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton addButton = new JButton("Add Client");
        JButton updateButton = new JButton("Update Client");
        JButton deleteButton = new JButton("Delete Client");
        JButton clearButton = new JButton("Clear Form");

        addButton.addActionListener(e -> addClient());
        updateButton.addActionListener(e -> updateClient());
        deleteButton.addActionListener(e -> deleteClient());
        clearButton.addActionListener(e -> clearForm());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        return formPanel;
    }

    public ClientPanel(ClientService clientService) {
        // Initialize all final fields first
        this.clientService = clientService;
        String[] columnNames = {"ID", "First Name", "Last Name", "Address", "Phone", "Email"};
        this.tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.clientTable = new JTable(tableModel);
        this.firstNameField = new JTextField(20);
        this.lastNameField = new JTextField(20);
        this.addressField = new JTextField(20);
        this.phoneField = new JTextField(20);
        this.emailField = new JTextField(20);

        // Setup layout
        this.setLayout(new BorderLayout(10, 10));

        // Create form panel with validation
        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.NORTH);

        // Setup table UI
        clientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        clientTable.getTableHeader().setReorderingAllowed(false);
        JScrollPane scrollPane = new JScrollPane(clientTable);
        add(scrollPane, BorderLayout.CENTER);

        // Add selection listener with improved error handling
        clientTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && clientTable.getSelectedRow() != -1) {
                try {
                    selectedClientId = (String) clientTable.getValueAt(clientTable.getSelectedRow(), 0);
                    clientService.findClientById(selectedClientId)
                        .ifPresentOrElse(
                            this::populateForm,
                            () -> handleError("Client not found"));
                } catch (Exception ex) {
                    handleError("Error loading client details: " + ex.getMessage());
                }
            }
        });

        // Add keyboard shortcuts
        setupKeyboardShortcuts();

        // Initial table load
        refreshTable();
    }

    private void setupKeyboardShortcuts() {
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        try {
            // Add client - Ctrl+A
            KeyStroke addStroke = KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
            inputMap.put(addStroke, "addClient");
            actionMap.put("addClient", new AbstractAction("Add Client") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    addClient();
                }
            });

            // Clear form - Escape
            KeyStroke clearStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
            inputMap.put(clearStroke, "clearForm");
            actionMap.put("clearForm", new AbstractAction("Clear Form") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    clearForm();
                }
            });
        } catch (Exception ex) {
            handleError("Error setting up keyboard shortcuts: " + ex.getMessage());
        }
    }

    private void populateForm(Client client) {
        firstNameField.setText(client.getFirstName());
        lastNameField.setText(client.getLastName());
        addressField.setText(client.getAddress());
        phoneField.setText(client.getPhoneNumber());
        emailField.setText(client.getEmail());
    }

    private boolean validateInput() {
        if (firstNameField.getText().trim().isEmpty() ||
            lastNameField.getText().trim().isEmpty() ||
            addressField.getText().trim().isEmpty() ||
            phoneField.getText().trim().isEmpty() ||
            emailField.getText().trim().isEmpty()) {
            handleError("All fields are required");
            return false;
        }

        if (!phoneField.getText().matches("\\d{10}")) {
            handleError("Phone number must be 10 digits");
            return false;
        }

        if (!emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            handleError("Invalid email format");
            return false;
        }

        return true;
    }

    private void handleError(String message) {
        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE));
    }

    private void addClient() {
        if (!validateInput()) return;

        try {
            SwingUtilities.invokeLater(() -> {
                try {
                    clientService.addClient(
                        firstNameField.getText().trim(),
                        lastNameField.getText().trim(),
                        addressField.getText().trim(),
                        phoneField.getText().trim(),
                        emailField.getText().trim()
                    );
                    clearForm();
                    refreshTable();
                } catch (Exception e) {
                    handleError("Error adding client: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            handleError("Error adding client: " + e.getMessage());
        }
    }

    private void updateClient() {
        if (selectedClientId == null) {
            handleError("Please select a client to update");
            return;
        }

        if (!validateInput()) return;

        try {
            SwingUtilities.invokeLater(() -> {
                try {
                    clientService.updateClient(
                        selectedClientId,
                        firstNameField.getText().trim(),
                        lastNameField.getText().trim(),
                        addressField.getText().trim(),
                        phoneField.getText().trim(),
                        emailField.getText().trim()
                    );
                    clearForm();
                    refreshTable();
                } catch (Exception e) {
                    handleError("Error updating client: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            handleError("Error updating client: " + e.getMessage());
        }
    }

    private void deleteClient() {
        if (selectedClientId == null) {
            handleError("Please select a client to delete");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this client?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                SwingUtilities.invokeLater(() -> {
                    try {
                        clientService.deleteClient(selectedClientId);
                        clearForm();
                        refreshTable();
                    } catch (Exception e) {
                        handleError("Error deleting client: " + e.getMessage());
                    }
                });
            } catch (Exception e) {
                handleError("Error deleting client: " + e.getMessage());
            }
        }
    }

    private void refreshTable() {
        try {
            SwingUtilities.invokeLater(() -> {
                try {
                    tableModel.setRowCount(0);
                    List<Client> clients = clientService.getAllClients();
                    for (Client client : clients) {
                        Object[] row = {
                            client.getId(),
                            client.getFirstName(),
                            client.getLastName(),
                            client.getAddress(),
                            client.getPhoneNumber(),
                            client.getEmail()
                        };
                        tableModel.addRow(row);
                    }
                } catch (Exception e) {
                    handleError("Error refreshing table: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            handleError("Error refreshing table: " + e.getMessage());
        }
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

    private void clearForm() {
        firstNameField.setText("");
        lastNameField.setText("");
        addressField.setText("");
        phoneField.setText("");
        emailField.setText("");
        selectedClientId = null;
        clientTable.clearSelection();
    }


}