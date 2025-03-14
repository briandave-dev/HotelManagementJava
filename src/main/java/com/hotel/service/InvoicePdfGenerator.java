package com.hotel.service;

import com.hotel.model.Client;
import com.hotel.model.Invoice;
import com.hotel.model.Reservation;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class InvoicePdfGenerator {
    
    private static final String DEST_FOLDER = "generated-pdfs/";
    private static final DeviceRgb HEADER_COLOR = new DeviceRgb(66, 133, 244);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    /**
     * Génère un PDF à partir d'un objet Invoice
     * @param invoice L'objet Invoice contenant les informations de la facture
     * @return Le chemin du fichier PDF généré
     */
    public String generateInvoicePdf(Invoice invoice) throws IOException {
        // Créer le dossier de destination s'il n'existe pas
        File destFolder = new File(DEST_FOLDER);
        if (!destFolder.exists()) {
            destFolder.mkdirs();
        }
        
        // Définir le chemin du fichier de sortie
        String fileName = "Invoice_" + invoice.getId() + ".pdf";
        String filePath = DEST_FOLDER + fileName;
        
        // Initialiser le document PDF
        PdfWriter writer = new PdfWriter(new FileOutputStream(filePath));
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(36, 36, 36, 36);
        
        // Définir les polices
        PdfFont normalFont = PdfFontFactory.createFont("Helvetica");
        PdfFont boldFont = PdfFontFactory.createFont("Helvetica-Bold");
        
        // Ajouter le titre
        Paragraph title = new Paragraph("FACTURE")
                .setFont(boldFont)
                .setFontSize(24)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(title);
        
        // Ajouter les informations de l'hôtel
        Paragraph hotelInfo = new Paragraph("NOM DE LHOTEL")
                .setFont(boldFont)
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(hotelInfo);
        
        Paragraph hotelAddress = new Paragraph("ENSPD, Cameroun\nTél: +237 699 999 999\nEmail: briandave.dev@gmail.com\nfankamnga@gmail.com")
                .setFont(normalFont)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(hotelAddress);
        
        // Ajouter les détails de la facture
        Table invoiceDetailsTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);
        
        invoiceDetailsTable.addCell(createCell("Numéro de Facture:", normalFont, 10, TextAlignment.LEFT));
        invoiceDetailsTable.addCell(createCell(invoice.getId(), boldFont, 10, TextAlignment.RIGHT));
        
        invoiceDetailsTable.addCell(createCell("Date de Génération:", normalFont, 10, TextAlignment.LEFT));
        invoiceDetailsTable.addCell(createCell(invoice.getGenerationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), boldFont, 10, TextAlignment.RIGHT));
        
        invoiceDetailsTable.addCell(createCell("Statut:", normalFont, 10, TextAlignment.LEFT));
        invoiceDetailsTable.addCell(createCell(invoice.isPaid() ? "PAYÉE" : "EN ATTENTE DE PAIEMENT", boldFont, 10, TextAlignment.RIGHT));
        
        document.add(invoiceDetailsTable);
        
        // Ajouter les informations du client
        Paragraph clientTitle = new Paragraph("INFORMATIONS CLIENT")
                .setFont(boldFont)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.LEFT)
                .setMarginBottom(10);
        document.add(clientTitle);
        
        Client client = invoice.getReservation().getClient();
        Table clientTable = new Table(UnitValue.createPercentArray(new float[]{1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);
        
        clientTable.addCell(createCell("Nom: " + client.getFirstName() + " " + client.getLastName(), normalFont, 10, TextAlignment.LEFT));
        clientTable.addCell(createCell("Email: " + client.getEmail(), normalFont, 10, TextAlignment.LEFT));
        clientTable.addCell(createCell("Téléphone: " + client.getPhoneNumber(), normalFont, 10, TextAlignment.LEFT));
        
        document.add(clientTable);
        
        // Ajouter les détails de la réservation
        Paragraph reservationTitle = new Paragraph("DÉTAILS DE LA RÉSERVATION")
                .setFont(boldFont)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.LEFT)
                .setMarginBottom(10);
        document.add(reservationTitle);
        
        Reservation reservation = invoice.getReservation();
        Table reservationTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);
        
        reservationTable.addCell(createCell("Numéro de Réservation:", normalFont, 10, TextAlignment.LEFT));
        reservationTable.addCell(createCell(reservation.getId(), boldFont, 10, TextAlignment.RIGHT));
        
        reservationTable.addCell(createCell("Chambre:", normalFont, 10, TextAlignment.LEFT));
        reservationTable.addCell(createCell(reservation.getRoom().getNumber() + " - " + reservation.getRoom().getCategory().getDisplayName(), boldFont, 10, TextAlignment.RIGHT));
        
        reservationTable.addCell(createCell("Date d'arrivée:", normalFont, 10, TextAlignment.LEFT));
        reservationTable.addCell(createCell(reservation.getCheckInDate().format(DATE_FORMATTER), boldFont, 10, TextAlignment.RIGHT));
        
        reservationTable.addCell(createCell("Date de départ:", normalFont, 10, TextAlignment.LEFT));
        reservationTable.addCell(createCell(reservation.getCheckOutDate().format(DATE_FORMATTER), boldFont, 10, TextAlignment.RIGHT));
        
        reservationTable.addCell(createCell("Durée du séjour:", normalFont, 10, TextAlignment.LEFT));
        long days = java.time.temporal.ChronoUnit.DAYS.between(reservation.getCheckInDate(), reservation.getCheckOutDate());
        reservationTable.addCell(createCell(days + " nuit(s)", boldFont, 10, TextAlignment.RIGHT));
        
        document.add(reservationTable);
        
        // Ajouter le récapitulatif des frais
        Paragraph costsTitle = new Paragraph("RÉCAPITULATIF")
                .setFont(boldFont)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.LEFT)
                .setMarginBottom(10);
        document.add(costsTitle);
        
        Table costsTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);
        
        // En-tête
        costsTable.addHeaderCell(createHeaderCell("Description", boldFont, 10));
        costsTable.addHeaderCell(createHeaderCell("Montant", boldFont, 10));
        
        // Contenu
        costsTable.addCell(createCell("Hébergement - " + days + " nuit(s) x " + String.format("%.2f", reservation.getRoom().getRatePerNight()) + " $", normalFont, 10, TextAlignment.LEFT));
        costsTable.addCell(createCell(String.format("%.2f $", invoice.getSubtotal()), normalFont, 10, TextAlignment.RIGHT));
        
        costsTable.addCell(createCell("Sous-total", boldFont, 10, TextAlignment.LEFT));
        costsTable.addCell(createCell(String.format("%.2f $", invoice.getSubtotal()), boldFont, 10, TextAlignment.RIGHT));
        
        costsTable.addCell(createCell("TVA (" + String.format("%.1f", (invoice.getTax() / invoice.getSubtotal()) * 100) + "%)", normalFont, 10, TextAlignment.LEFT));
        costsTable.addCell(createCell(String.format("%.2f $", invoice.getTax()), normalFont, 10, TextAlignment.RIGHT));
        
        Cell totalLabelCell = createCell("TOTAL", boldFont, 12, TextAlignment.LEFT);
        totalLabelCell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
        costsTable.addCell(totalLabelCell);
        
        Cell totalValueCell = createCell(String.format("%.2f $", invoice.getTotal()), boldFont, 12, TextAlignment.RIGHT);
        totalValueCell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
        costsTable.addCell(totalValueCell);
        
        document.add(costsTable);
        
        // Ajouter les notes et conditions
        Paragraph notesTitle = new Paragraph("CONDITIONS & NOTES")
                .setFont(boldFont)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.LEFT)
                .setMarginBottom(10);
        document.add(notesTitle);
        
        Paragraph notes = new Paragraph(
                "1. Paiement exigible à réception de la facture.\n" +
                "2. Nous acceptons les paiements par carte de crédit, virement bancaire ou espèces.\n" +
                "3. Veuillez conserver cette facture pour votre comptabilité.\n" +
                "4. En cas de question concernant cette facture, veuillez contacter notre service client."
        )
                .setFont(normalFont)
                .setFontSize(9)
                .setTextAlignment(TextAlignment.LEFT)
                .setMarginBottom(20);
        document.add(notes);
        
        // Ajouter le pied de page
        Paragraph footer = new Paragraph("Merci de votre confiance. Au plaisir de vous accueillir à nouveau.")
                .setFont(normalFont)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20);
        document.add(footer);
        
        // Fermer le document
        document.close();
        
        return filePath;
    }
    
    /**
     * Crée une cellule de tableau avec un contenu et un style
     */
    private Cell createCell(String content, PdfFont font, float fontSize, TextAlignment alignment) {
        Cell cell = new Cell()
                .setPadding(5)
                .setBorder(null)
                .add(new Paragraph(content)
                        .setFont(font)
                        .setFontSize(fontSize)
                        .setTextAlignment(alignment));
        return cell;
    }
    
    /**
     * Crée une cellule d'en-tête pour les tableaux
     */
    private Cell createHeaderCell(String content, PdfFont font, float fontSize) {
        Cell cell = new Cell()
                .setPadding(5)
                .setBackgroundColor(HEADER_COLOR)
                .setBorder(new SolidBorder(ColorConstants.WHITE, 1))
                .add(new Paragraph(content)
                        .setFont(font)
                        .setFontSize(fontSize)
                        .setFontColor(ColorConstants.WHITE)
                        .setTextAlignment(TextAlignment.CENTER));
        return cell;
    }
}