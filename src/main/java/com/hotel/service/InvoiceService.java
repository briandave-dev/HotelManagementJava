package com.hotel.service;

import com.hotel.model.Invoice;
import com.hotel.model.Reservation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InvoiceService {
    private final List<Invoice> invoices;
    private final double defaultTaxRate;

    public InvoiceService(double defaultTaxRate) {
        this.invoices = new ArrayList<>();
        this.defaultTaxRate = defaultTaxRate;
    }

    public Invoice generateInvoice(Reservation reservation) {
        Invoice invoice = new Invoice(reservation, defaultTaxRate);
        invoices.add(invoice);
        return invoice;
    }

    public void addAdditionalService(String invoiceId, String description, double price) {
        findInvoiceById(invoiceId)
                .ifPresent(invoice -> invoice.addAdditionalService(description, price));
    }

    public void markAsPaid(String invoiceId) {
        findInvoiceById(invoiceId)
                .ifPresent(invoice -> invoice.setPaid(true));
    }

    public List<Invoice> getAllInvoices() {
        return new ArrayList<>(invoices);
    }

    public List<Invoice> getUnpaidInvoices() {
        return invoices.stream()
                .filter(invoice -> !invoice.isPaid())
                .collect(java.util.stream.Collectors.toList());
    }

    public Optional<Invoice> findInvoiceById(String invoiceId) {
        return invoices.stream()
                .filter(invoice -> invoice.getId().equals(invoiceId))
                .findFirst();
    }

    public void updateInvoice(Invoice invoice) {
        int index = -1;
        for (int i = 0; i < invoices.size(); i++) {
            if (invoices.get(i).getId().equals(invoice.getId())) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            invoices.set(index, invoice);
        }
    }

    public List<Invoice> getInvoicesForReservation(String reservationId) {
        return invoices.stream()
                .filter(invoice -> invoice.getReservation().getId().equals(reservationId))
                .collect(java.util.stream.Collectors.toList());
    }
}