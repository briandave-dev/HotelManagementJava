package com.hotel.model;

public  class AdditionalService {
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