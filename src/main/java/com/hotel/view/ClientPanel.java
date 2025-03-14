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
    
    // Define consistent colors
    private final Color PRIMARY_BLUE = new Color(51, 122, 183);
    private final Color DANGER_RED = new Color(220, 53, 69);
    private final Color SUCCESS_GREEN = new Color(40, 167, 69);

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

        // Fix header styling - ensure it's blue with white text
        clientTable.getTableHeader().setBackground(PRIMARY_BLUE);
        // clientTable.getTableHeader().setForeground(Color.WHITE);
        clientTable.getTableHeader().setFont(clientTable.getTableHeader().getFont().deriveFont(Font.BOLD));
        
        // Make sure the background color is retained by making it opaque
        clientTable.getTableHeader().setOpaque(true);
        
        // Disable the default blue selection highlight
        clientTable.setSelectionBackground(clientTable.getBackground());
        clientTable.setSelectionForeground(clientTable.getForeground());
        
        // Set row height to better accommodate buttons
        clientTable.setRowHeight(30);

        // Configure the Actions column with proper button handling
        clientTable.getColumn("Actions").setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            panel.setBackground(table.getBackground()); // Always use default background
            
            if (value instanceof JPanel) {
                return (JPanel) value;
            }
            
            String clientId = (String) tableModel.getValueAt(row, 0);
            JButton editBtn = createButton("Edit");
            JButton copyBtn = createButton("Copy ID");
            JButton deleteBtn = createButton("Delete");
            deleteBtn.setBackground(DANGER_RED); // Red color for delete button
            
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
            panel.setBackground(table.getBackground()); // Keep default background instead of selection color
            
            // Create buttons with the same styling as in your renderer
            JButton editBtn = createButton("Edit");
            JButton copyBtn = createButton("Copy ID");
            JButton deleteBtn = createButton("Delete");
            deleteBtn.setBackground(DANGER_RED); // Red color for delete button
            
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
        button.setBackground(PRIMARY_BLUE);
        // button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true); // This is critical to ensure the background color is shown
        button.setContentAreaFilled(true); // Make sure content area is filled
        
        // Remove any rollover/hover effects
        button.getModel().addChangeListener(e -> {
            if (button.getModel().isRollover()) {
                button.setBackground(button.getBackground().darker());
            } else {
                button.setBackground(button.getBackground() == DANGER_RED ? DANGER_RED : PRIMARY_BLUE);
            }
        });
        
        return button;
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Create form panel with rounded borders
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        formPanel.setBackground(new Color(245, 245, 245));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Add form fields with improved styling
        addFormField(formPanel, "First Name:", firstNameField, gbc, 0);
        addFormField(formPanel, "Last Name:", lastNameField, gbc, 1);
        addFormField(formPanel, "Address:", addressField, gbc, 2);
        addFormField(formPanel, "Phone:", phoneField, gbc, 3);
        addFormField(formPanel, "Email:", emailField, gbc, 4);

        // Add buttons with improved styling
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);
        
        JButton addButton = createStyledButton("Add Client", SUCCESS_GREEN);
        JButton clearButton = createStyledButton("Clear Form", new Color(108, 117, 125));

        addButton.addActionListener(e -> addClient());
        clearButton.addActionListener(e -> clearForm());

        buttonPanel.add(addButton);
        buttonPanel.add(clearButton);

        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(buttonPanel, gbc);

        // Create a wrapper panel to center the form
        JPanel wrapperPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrapperPanel.add(formPanel);
        wrapperPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Add title
        JLabel titleLabel = new JLabel("Client Management", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Create a scroll pane with styled table
        JScrollPane scrollPane = new JScrollPane(clientTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        
        // Create a panel for the table section
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Create a main content panel with form at top and table below
        JPanel contentPanel = new JPanel(new BorderLayout(0, 20));
        contentPanel.add(wrapperPanel, BorderLayout.NORTH);
        contentPanel.add(tablePanel, BorderLayout.CENTER);

        // Add components to panel
        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
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
            JButton saveButton = createStyledButton("Save", SUCCESS_GREEN);
            JButton cancelButton = createStyledButton("Cancel", new Color(108, 117, 125));

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
                showToast("Client updated successfully!");
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
            showToast("Client added successfully!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding client: " + e.getMessage());
        }
    }

    private void addFormField(JPanel panel, String labelText, JTextField field, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 30));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        panel.add(field, gbc);
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(120, 35));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true); // Ensure background color is shown
        button.setContentAreaFilled(true); // Make sure content area is filled
        
        // Remove hover effect by maintaining color
        button.getModel().addChangeListener(e -> {
            if (button.getModel().isRollover()) {
                button.setBackground(bgColor.darker());
            } else {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }

    private void copyClientId(int row) {
        String clientId = (String) tableModel.getValueAt(row, 0);
        StringSelection selection = new StringSelection(clientId);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
        showToast("Client ID copied to clipboard!");
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
            showToast("Client deleted successfully!");
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        clientService.getAllClients().forEach(client -> {
            // Create empty placeholder for actions column - the actual buttons will be created by the renderer
            Object[] row = {
                client.getId(),
                client.getFirstName(),
                client.getLastName(),
                client.getAddress(),
                client.getPhoneNumber(),
                client.getEmail(),
                null // This will be replaced by our custom renderer
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