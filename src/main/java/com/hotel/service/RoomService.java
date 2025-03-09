package com.hotel.service;

import com.hotel.model.Room;
import com.hotel.model.RoomCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RoomService {
    private final List<Room> rooms;

    public RoomService() {
        this.rooms = new ArrayList<>();
    }

    public void addRoom(String number, RoomCategory category, double ratePerNight, String amenities) {
        if (findRoomByNumber(number).isPresent()) {
            throw new IllegalArgumentException("Room number already exists");
        }
        rooms.add(new Room(number, category, ratePerNight, amenities));
    }

    public void updateRoom(String number, RoomCategory category, double ratePerNight, String amenities) {
        Room room = findRoomByNumber(number)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        
        room.setCategory(category);
        room.setRatePerNight(ratePerNight);
        room.setAmenities(amenities);
    }

    public void deleteRoom(String number) {
        rooms.removeIf(room -> room.getNumber().equals(number));
    }

    public List<Room> getAllRooms() {
        return new ArrayList<>(rooms);
    }

    public List<Room> getAvailableRooms() {
        return rooms.stream()
                .filter(room -> !room.isOccupied())
                .collect(Collectors.toList());
    }

    public List<Room> getRoomsByCategory(RoomCategory category) {
        return rooms.stream()
                .filter(room -> room.getCategory() == category)
                .collect(Collectors.toList());
    }

    public Optional<Room> findRoomByNumber(String number) {
        return rooms.stream()
                .filter(room -> room.getNumber().equals(number))
                .findFirst();
    }
}