package com.hotel.util;

import com.hotel.model.Invoice;
import com.hotel.model.Reservation;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class PDFGenerator {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String INVOICE_DIRECTORY = System.getProperty("user.home") + File.separator + "hotel_management" + File.separator + "invoices";

    public static String generateInvoicePDF(Invoice invoice) {
        // Validate invoice and reservation
        if (invoice == null) {
            throw new IllegalArgumentException("Invoice cannot be null");
        }
        
        Reservation reservation = invoice.getReservation();
        if (reservation == null) {
            throw new IllegalArgumentException("Invoice must be associated with a reservation");
        }

        Document document = null;
        try {
            // Create invoices directory if it doesn't exist
            File directory = new File(INVOICE_DIRECTORY);
            if (!directory.exists() && !directory.mkdirs()) {
                throw new RuntimeException("Failed to create invoice directory: " + INVOICE_DIRECTORY);
            }

            String fileName = INVOICE_DIRECTORY + File.separator + "invoice_" + invoice.getId() + ".pdf";
            document = new Document();
            PdfWriter.getInstance(document, new java.io.FileOutputStream(fileName));
            document.open();

            // Add header with styling
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, Font.NORMAL);
            Paragraph header = new Paragraph("Hotel Management System", headerFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            // Add invoice details with styling
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            document.add(new Paragraph("\nInvoice Details", titleFont));
            document.add(new Paragraph("Invoice ID: " + invoice.getId(), normalFont));
            document.add(new Paragraph("Reservation ID: " + reservation.getId(), normalFont));
            document.add(new Paragraph("Generation Date: " + invoice.getGenerationDate().format(DATE_FORMATTER), normalFont));

            // Add reservation details
            document.add(new Paragraph("\nReservation Details", titleFont));
            document.add(new Paragraph("Client: " + reservation.getClient().getName(), normalFont));
            document.add(new Paragraph("Room: " + reservation.getRoom().getNumber(), normalFont));
            document.add(new Paragraph("Check-in Date: " + reservation.getCheckInDate().format(DATE_FORMATTER), normalFont));
            document.add(new Paragraph("Check-out Date: " + reservation.getCheckOutDate().format(DATE_FORMATTER), normalFont));

            // Create table for charges
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(20);

            // Add table headers
            PdfPCell headerCell1 = new PdfPCell(new Paragraph("Description", titleFont));
            PdfPCell headerCell2 = new PdfPCell(new Paragraph("Amount", titleFont));
            headerCell1.setBackgroundColor(new java.awt.Color(52, 152, 219));
            headerCell2.setBackgroundColor(new java.awt.Color(52, 152, 219));
            headerCell1.setPadding(5);
            headerCell2.setPadding(5);
            table.addCell(headerCell1);
            table.addCell(headerCell2);

            // Add room charge
            table.addCell(new Paragraph("Room Charge", normalFont));
            table.addCell(new Paragraph(String.format(Locale.US, "$%.2f", invoice.getSubtotal()), normalFont));

            // Add additional services
            for (Invoice.AdditionalService service : invoice.getAdditionalServices()) {
                table.addCell(new Paragraph(service.getDescription(), normalFont));
                table.addCell(new Paragraph(String.format(Locale.US, "$%.2f", service.getPrice()), normalFont));
            }

            // Add tax and total
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            table.addCell(new Paragraph("Tax", boldFont));
            table.addCell(new Paragraph(String.format(Locale.US, "$%.2f", invoice.getTax()), boldFont));
            table.addCell(new Paragraph("Total", boldFont));
            table.addCell(new Paragraph(String.format(Locale.US, "$%.2f", invoice.getTotal()), boldFont));

            document.add(table);

            // Add payment status
            Font statusFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            statusFont.setColor(invoice.isPaid() ? new java.awt.Color(46, 204, 113) : new java.awt.Color(231, 76, 60));
            Paragraph status = new Paragraph("\nPayment Status: " + (invoice.isPaid() ? "Paid" : "Unpaid"), statusFont);
            status.setAlignment(Element.ALIGN_RIGHT);
            document.add(status);

            document.close();
            return fileName;
        } catch (Exception e) {
            if (document != null) {
                try {
                    document.close();
                } catch (Exception closeException) {
                    // Log the exception if needed
                }
            }
            throw new RuntimeException("Error generating PDF invoice: " + e.getMessage(), e);
        }
    }
}