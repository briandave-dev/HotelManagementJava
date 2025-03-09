package com.hotel.model;

public enum RoomCategory {
    SINGLE("Single Room", 1),
    DOUBLE("Double Room", 2),
    SUITE("Suite", 4);

    private final String displayName;
    private final int capacity;

    RoomCategory(String displayName, int capacity) {
        this.displayName = displayName;
        this.capacity = capacity;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getCapacity() {
        return capacity;
    }

    @Override
    public String toString() {
        return displayName;
    }
}