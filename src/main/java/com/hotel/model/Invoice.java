package com.hotel.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Invoice {
    private final String id;
    private final Reservation reservation;
    private final LocalDateTime generationDate;
    private final List<AdditionalService> additionalServices;
    private final double subtotal;
    private final double tax;
    private double total;
    private boolean isPaid;

    public Invoice(Reservation reservation, double taxRate) {
        this.id = UUID.randomUUID().toString();
        this.reservation = reservation;
        this.generationDate = LocalDateTime.now();
        this.additionalServices = new ArrayList<>();
        this.subtotal = reservation.getTotalPrice();
        this.tax = subtotal * taxRate;
        this.total = subtotal + tax;
        this.isPaid = false;
    }

    public void addAdditionalService(String description, double price) {
        additionalServices.add(new AdditionalService(description, price));
        recalculateTotal();
    }

    private void recalculateTotal() {
        double servicesTotal = additionalServices.stream()
                .mapToDouble(AdditionalService::getPrice)
                .sum();
        this.total = subtotal + tax + servicesTotal;
    }

    public String getId() {
        return id;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public LocalDateTime getGenerationDate() {
        return generationDate;
    }

    public List<AdditionalService> getAdditionalServices() {
        return new ArrayList<>(additionalServices);
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getTax() {
        return tax;
    }

    public double getTotal() {
        return total;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    private static class AdditionalService {
        private final String description;
        private final double price;

        public AdditionalService(String description, double price) {
            this.description = description;
            this.price = price;
        }

        public String getDescription() {
            return description;
        }

        public double getPrice() {
            return price;
        }
    }
}