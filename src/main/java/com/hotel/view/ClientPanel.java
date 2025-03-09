package com.hotel.view;

import com.hotel.model.Client;
import com.hotel.service.ClientService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
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
    private JDialog editDialog;

    public ClientPanel(ClientService clientService) {
        this.clientService = clientService;
        String[] columnNames = {"ID", "First Name", "Last Name", "Address", "Phone", "Email", "Actions"};
        this.tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == getColumnCount() - 1;
            }
        };
        this.clientTable = new JTable(tableModel);
        this.firstNameField = new JTextField(20);
        this.lastNameField = new JTextField(20);
        this.addressField = new JTextField(20);
        this.phoneField = new JTextField(20);
        this.emailField = new JTextField(20);

        setupTable();
        setupLayout();
        refreshTable();
    }

    private void setupTable() {
        clientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        clientTable.getTableHeader().setReorderingAllowed(false);

        // Set table header appearance
        clientTable.getTableHeader().setBackground(new Color(51, 122, 183));
        clientTable.getTableHeader().setForeground(Color.WHITE);
        clientTable.getTableHeader().setFont(clientTable.getTableHeader().getFont().deriveFont(Font.BOLD));
        
        // Set row height to better accommodate buttons
        clientTable.setRowHeight(30);

        // Configure the Actions column with proper button handling
        clientTable.getColumn("Actions").setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            
            if (value instanceof JPanel) {
                return (JPanel) value;
            }
            
            String clientId = (String) tableModel.getValueAt(row, 0);
            JButton editBtn = createButton("Edit");
            JButton copyBtn = createButton("Copy ID");
            JButton deleteBtn = createButton("Delete");
            deleteBtn.setBackground(new Color(220, 53, 69)); // Red color for delete button
            
            // Set fixed size for buttons
            Dimension buttonSize = new Dimension(60, 25);
            editBtn.setPreferredSize(buttonSize);
            copyBtn.setPreferredSize(buttonSize);
            deleteBtn.setPreferredSize(buttonSize);
            
            editBtn.addActionListener(e -> showEditDialog(row));
            copyBtn.addActionListener(e -> copyClientId(row));
            deleteBtn.addActionListener(e -> deleteClient(row));
            
            panel.add(editBtn);
            panel.add(copyBtn);
            panel.add(deleteBtn);
            
            return panel;
        });

        // Add button editor for handling button clicks
        clientTable.getColumn("Actions").setCellEditor(new ButtonEditor(clientTable));
        
        // Set preferred column widths
        clientTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        clientTable.getColumnModel().getColumn(6).setPreferredWidth(150); // Actions
    }

    class ButtonEditor extends DefaultCellEditor {
        private JPanel panel;
        private String clientId;
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
            this.clientId = (String) table.getModel().getValueAt(row, 0);
            
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
            ClientPanel clientPanel = (ClientPanel) SwingUtilities.getAncestorOfClass(ClientPanel.class, table);
            if (clientPanel != null) {
                editBtn.addActionListener(e -> {
                    // Stop editing to commit the cell first
                    stopCellEditing();
                    clientPanel.showEditDialog(row);
                });
                copyBtn.addActionListener(e -> {
                    stopCellEditing();
                    clientPanel.copyClientId(row);
                });
                deleteBtn.addActionListener(e -> {
                    stopCellEditing();
                    clientPanel.deleteClient(row);
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

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));

        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        // Add form fields
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("First Name:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(firstNameField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Last Name:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(lastNameField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Address:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(addressField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Phone:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(phoneField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Email:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(emailField, gbc);

        // Add buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton addButton = new JButton("Add Client");
        JButton clearButton = new JButton("Clear Form");

        addButton.addActionListener(e -> addClient());
        clearButton.addActionListener(e -> clearForm());

        buttonPanel.add(addButton);
        buttonPanel.add(clearButton);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.PAGE_END;
        formPanel.add(buttonPanel, gbc);

        // Create a wrapper panel to center the form
        JPanel wrapperPanel = new JPanel(new GridBagLayout());
        wrapperPanel.add(formPanel, new GridBagConstraints());

        // Add components to panel
        add(wrapperPanel, BorderLayout.NORTH);
        add(new JScrollPane(clientTable), BorderLayout.CENTER);
    }

    private void showEditDialog(int row) {
        editDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Client", true);
        editDialog.setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Get client data
        String clientId = (String) tableModel.getValueAt(row, 0);
        Client client = clientService.findClientById(clientId).orElse(null);
        if (client != null) {
            // Create form fields
            JTextField firstNameField = new JTextField(client.getFirstName());
            JTextField lastNameField = new JTextField(client.getLastName());
            JTextField addressField = new JTextField(client.getAddress());
            JTextField phoneField = new JTextField(client.getPhoneNumber());
            JTextField emailField = new JTextField(client.getEmail());

            // Add fields to form with proper spacing
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 0.0;
            formPanel.add(new JLabel("First Name:"), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            formPanel.add(firstNameField, gbc);

            gbc.gridy++;
            gbc.gridx = 0;
            gbc.weightx = 0.0;
            formPanel.add(new JLabel("Last Name:"), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            formPanel.add(lastNameField, gbc);

            gbc.gridy++;
            gbc.gridx = 0;
            gbc.weightx = 0.0;
            formPanel.add(new JLabel("Address:"), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            formPanel.add(addressField, gbc);

            gbc.gridy++;
            gbc.gridx = 0;
            gbc.weightx = 0.0;
            formPanel.add(new JLabel("Phone:"), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            formPanel.add(phoneField, gbc);

            gbc.gridy++;
            gbc.gridx = 0;
            gbc.weightx = 0.0;
            formPanel.add(new JLabel("Email:"), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            formPanel.add(emailField, gbc);

            // Add buttons
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton saveButton = new JButton("Save");
            JButton cancelButton = new JButton("Cancel");

            saveButton.addActionListener(e -> {
                // Update client
                client.setFirstName(firstNameField.getText());
                client.setLastName(lastNameField.getText());
                client.setAddress(addressField.getText());
                client.setPhoneNumber(phoneField.getText());
                client.setEmail(emailField.getText());

                clientService.updateClient(client.getId(), client.getFirstName(), client.getLastName(), 
                    client.getAddress(), client.getPhoneNumber(), client.getEmail());
                editDialog.dispose();
                refreshTable();
            });

            cancelButton.addActionListener(e -> editDialog.dispose());

            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);

            editDialog.add(formPanel, BorderLayout.CENTER);
            editDialog.add(buttonPanel, BorderLayout.SOUTH);
        }

        editDialog.pack();
        editDialog.setLocationRelativeTo(this);
        editDialog.setVisible(true);
    }

    private void addClient() {
        // Validate all required fields
        if (firstNameField.getText().trim().isEmpty() ||
            lastNameField.getText().trim().isEmpty() ||
            addressField.getText().trim().isEmpty() ||
            phoneField.getText().trim().isEmpty() ||
            emailField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Client newClient = new Client(
                firstNameField.getText().trim(),
                lastNameField.getText().trim(),
                addressField.getText().trim(),
                phoneField.getText().trim(),
                emailField.getText().trim()
            );
            clientService.addClient(newClient.getFirstName(), newClient.getLastName(),
                newClient.getAddress(), newClient.getPhoneNumber(), newClient.getEmail());
            clearForm();
            refreshTable();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding client: " + e.getMessage());
        }
    }

    private void addFormField(JPanel panel, String label, JComponent field, GridBagConstraints gbc) {
        gbc.gridx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
        gbc.gridy++;
    }

    private void copyClientId(int row) {
        String clientId = (String) tableModel.getValueAt(row, 0);
        StringSelection selection = new StringSelection(clientId);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
        JOptionPane.showMessageDialog(this, "Client ID copied to clipboard!", "Copy Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteClient(int row) {
        String clientId = (String) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete this client?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            clientService.deleteClient(clientId);
            refreshTable();
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        clientService.getAllClients().forEach(client -> {
            JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            
            // Create buttons with fixed size
            Dimension buttonSize = new Dimension(60, 25);
            JButton editBtn = createButton("Edit");
            JButton copyBtn = createButton("Copy ID");
            JButton deleteBtn = createButton("Delete");
            deleteBtn.setBackground(new Color(220, 53, 69));
            
            editBtn.setPreferredSize(buttonSize);
            copyBtn.setPreferredSize(buttonSize);
            deleteBtn.setPreferredSize(buttonSize);
            
            editBtn.addActionListener(e -> showEditDialog(tableModel.getRowCount()));
            copyBtn.addActionListener(e -> copyClientId(tableModel.getRowCount()));
            deleteBtn.addActionListener(e -> deleteClient(tableModel.getRowCount()));
            
            actionPanel.add(editBtn);
            actionPanel.add(copyBtn);
            actionPanel.add(deleteBtn);
            
            Object[] row = {
                client.getId(),
                client.getFirstName(),
                client.getLastName(),
                client.getAddress(),
                client.getPhoneNumber(),
                client.getEmail(),
                actionPanel
            };
            tableModel.addRow(row);
        });
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