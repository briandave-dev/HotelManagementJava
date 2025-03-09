package com.hotel.view;

import com.hotel.model.Invoice;
import com.hotel.service.InvoiceService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

public class InvoicePanel extends JPanel {
    private final InvoiceService invoiceService;
    private JTable invoiceTable;
    private DefaultTableModel tableModel;
    private JDialog editDialog;

    public InvoicePanel(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
        setLayout(new BorderLayout());
        initializeComponents();
    }

    private void initializeComponents() {
        // Create the table model with columns including Reservation ID and Actions
        String[] columns = {"Invoice ID", "Reservation ID", "Client", "Room", "Check-in", "Check-out", "Total", "Status", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == getColumnCount() - 1; // Only allow editing of Actions column
            }
        };

        // Create and configure the table
        invoiceTable = new JTable(tableModel);
        
        // Set table header appearance
        invoiceTable.getTableHeader().setBackground(new Color(51, 122, 183));
        invoiceTable.getTableHeader().setForeground(Color.WHITE);
        invoiceTable.getTableHeader().setFont(invoiceTable.getTableHeader().getFont().deriveFont(Font.BOLD));
        
        // Set row height to better accommodate buttons
        invoiceTable.setRowHeight(30);
        
        invoiceTable.getColumn("Actions").setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
            if (value instanceof JPanel) {
                return (JPanel) value;
            }
            
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            
            JButton editBtn = createButton("Edit");
            JButton copyBtn = createButton("Copy ID");
            JButton viewBtn = createButton("View");
            
            // Set fixed size for buttons
            Dimension buttonSize = new Dimension(60, 25);
            editBtn.setPreferredSize(buttonSize);
            copyBtn.setPreferredSize(buttonSize);
            viewBtn.setPreferredSize(buttonSize);
            
            editBtn.addActionListener(e -> showEditDialog(row));
            copyBtn.addActionListener(e -> copyInvoiceId(row));
            viewBtn.addActionListener(e -> viewInvoiceDetails(row));
            
            panel.add(editBtn);
            panel.add(copyBtn);
            panel.add(viewBtn);
            
            return panel;
        });

        // Set preferred column widths
        invoiceTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // Invoice ID
        invoiceTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Reservation ID
        invoiceTable.getColumnModel().getColumn(8).setPreferredWidth(150); // Actions

        JScrollPane scrollPane = new JScrollPane(invoiceTable);

        // Create buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshButton = new JButton("Refresh");
        JButton generatePdfButton = new JButton("Generate PDF");

        buttonPanel.add(refreshButton);
        buttonPanel.add(generatePdfButton);

        // Add action listeners
        refreshButton.addActionListener(e -> refreshInvoiceList());
        generatePdfButton.addActionListener(e -> generatePdf());

        // Add components to panel
        add(new JLabel("Invoices", SwingConstants.CENTER), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Initial load of invoices
        refreshInvoiceList();
    }

    private void showEditDialog(int row) {
        editDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Invoice", true);
        editDialog.setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Add form fields
        String invoiceId = (String) tableModel.getValueAt(row, 0);
        Invoice invoice = invoiceService.findInvoiceById(invoiceId).orElse(null);
        if (invoice != null) {
            // Add fields for editing
            JTextField statusField = new JTextField(invoice.isPaid() ? "Paid" : "Unpaid");
            addFormField(formPanel, "Status:", statusField, gbc);

            // Add buttons
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton saveButton = new JButton("Save");
            JButton cancelButton = new JButton("Cancel");

            saveButton.addActionListener(e -> {
                // Update invoice status
                invoice.setPaid(statusField.getText().equalsIgnoreCase("Paid"));
                invoiceService.updateInvoice(invoice);
                editDialog.dispose();
                refreshInvoiceList();
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

    private void addFormField(JPanel panel, String label, JComponent field, GridBagConstraints gbc) {
        gbc.gridx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
        gbc.gridy++;
    }

    private void copyInvoiceId(int row) {
        String invoiceId = (String) tableModel.getValueAt(row, 0);
        StringSelection selection = new StringSelection(invoiceId);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
        JOptionPane.showMessageDialog(this, "Invoice ID copied to clipboard!", "Copy Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void viewInvoiceDetails(int row) {
        String invoiceId = (String) tableModel.getValueAt(row, 0);
        Invoice invoice = invoiceService.findInvoiceById(invoiceId).orElse(null);
        if (invoice != null) {
            StringBuilder details = new StringBuilder();
            details.append("Invoice Details:\n")
                   .append("ID: ").append(invoice.getId()).append("\n")
                   .append("Reservation ID: ").append(invoice.getReservation().getId()).append("\n")
                   .append("Subtotal: $").append(String.format("%.2f", invoice.getSubtotal())).append("\n")
                   .append("Tax: $").append(String.format("%.2f", invoice.getTax())).append("\n")
                   .append("Total: $").append(String.format("%.2f", invoice.getTotal())).append("\n")
                   .append("Status: ").append(invoice.isPaid() ? "Paid" : "Unpaid").append("\n");

            JTextArea textArea = new JTextArea(details.toString());
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            JOptionPane.showMessageDialog(this, scrollPane, "Invoice Details", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void generatePdf() {
        int selectedRow = invoiceTable.getSelectedRow();
        if (selectedRow >= 0) {
            String invoiceId = (String) tableModel.getValueAt(selectedRow, 0);
            Invoice invoice = invoiceService.findInvoiceById(invoiceId).orElse(null);
            if (invoice != null) {
                // TODO: Implement PDF generation
                JOptionPane.showMessageDialog(this, "PDF generation will be implemented", "Generate PDF", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an invoice to generate PDF", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void refreshInvoiceList() {
        tableModel.setRowCount(0);
        invoiceService.getAllInvoices().forEach(invoice -> {
            Object[] row = {
                invoice.getId(),
                invoice.getReservation().getId(),
                invoice.getReservation().getClient().getFullName(),
                invoice.getReservation().getRoom().getNumber(),
                invoice.getReservation().getCheckInDate(),
                invoice.getReservation().getCheckOutDate(),
                String.format("$%.2f", invoice.getTotal()),
                invoice.isPaid() ? "Paid" : "Unpaid",
                createActionPanel(invoice.getId())
            };
            tableModel.addRow(row);
        });
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

    private JPanel createActionPanel(String invoiceId) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        JButton editBtn = createButton("Edit");
        JButton copyBtn = createButton("Copy ID");
        JButton viewBtn = createButton("View");

        // Add action listeners to the buttons
        editBtn.addActionListener(e -> {
            int row = findRowByInvoiceId(invoiceId);
            if (row >= 0) showEditDialog(row);
        });
        copyBtn.addActionListener(e -> {
            int row = findRowByInvoiceId(invoiceId);
            if (row >= 0) copyInvoiceId(row);
        });
        viewBtn.addActionListener(e -> {
            int row = findRowByInvoiceId(invoiceId);
            if (row >= 0) viewInvoiceDetails(row);
        });

        panel.add(editBtn);
        panel.add(copyBtn);
        panel.add(viewBtn);

        return panel;
    }

    private int findRowByInvoiceId(String invoiceId) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (invoiceId.equals(tableModel.getValueAt(i, 0))) {
                return i;
            }
        }
        return -1;
    }
}