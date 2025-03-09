package com.hotel.view;

import com.hotel.service.InvoiceService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class InvoicePanel extends JPanel {
    private final InvoiceService invoiceService;
    private JTable invoiceTable;
    private DefaultTableModel tableModel;

    public InvoicePanel(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
        setLayout(new BorderLayout());
        initializeComponents();
    }

    private void initializeComponents() {
        // Create the table model with columns
        String[] columns = {"Invoice ID", "Client", "Room", "Check-in", "Check-out", "Total", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };

        // Create and configure the table
        invoiceTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(invoiceTable);

        // Create buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton viewButton = new JButton("View Details");
        JButton generatePdfButton = new JButton("Generate PDF");
        JButton refreshButton = new JButton("Refresh");

        buttonPanel.add(viewButton);
        buttonPanel.add(generatePdfButton);
        buttonPanel.add(refreshButton);

        // Add action listeners
        viewButton.addActionListener(e -> viewInvoiceDetails());
        generatePdfButton.addActionListener(e -> generatePdf());
        refreshButton.addActionListener(e -> refreshInvoiceList());

        // Add components to panel
        add(new JLabel("Invoices", SwingConstants.CENTER), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Initial load of invoices
        refreshInvoiceList();
    }

    private void viewInvoiceDetails() {
        int selectedRow = invoiceTable.getSelectedRow();
        if (selectedRow >= 0) {
            // TODO: Implement invoice detail view
            JOptionPane.showMessageDialog(this,
                "Invoice details view will be implemented here",
                "Invoice Details",
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                "Please select an invoice to view",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
        }
    }

    private void generatePdf() {
        int selectedRow = invoiceTable.getSelectedRow();
        if (selectedRow >= 0) {
            // TODO: Implement PDF generation using iText
            JOptionPane.showMessageDialog(this,
                "PDF generation will be implemented here",
                "Generate PDF",
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                "Please select an invoice to generate PDF",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
        }
    }

    private void refreshInvoiceList() {
        // Clear existing rows
        tableModel.setRowCount(0);
        
        // TODO: Implement loading invoices from service
        // This will be implemented when the invoice service is complete
        // For now, we can add some dummy data
        Object[] dummyData = {"INV001", "John Doe", "101", "2024-01-01", "2024-01-03", "$300.00", "Paid"};
        tableModel.addRow(dummyData);
    }
}