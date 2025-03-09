package com.hotel.service;

import com.hotel.model.Client;
import com.hotel.model.Reservation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClientService {
    private final List<Client> clients;

    public ClientService() {
        this.clients = new ArrayList<>();
    }

    public void addClient(String firstName, String lastName, String address, String phoneNumber, String email) {
        clients.add(new Client(firstName, lastName, address, phoneNumber, email));
    }

    public void updateClient(String clientId, String firstName, String lastName, String address, String phoneNumber, String email) {
        Client client = findClientById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));
        
        client.setFirstName(firstName);
        client.setLastName(lastName);
        client.setAddress(address);
        client.setPhoneNumber(phoneNumber);
        client.setEmail(email);
    }

    public void deleteClient(String clientId) {
        clients.removeIf(client -> client.getId().equals(clientId));
    }

    public List<Client> getAllClients() {
        return new ArrayList<>(clients);
    }

    public Optional<Client> findClientById(String clientId) {
        return clients.stream()
                .filter(client -> client.getId().equals(clientId))
                .findFirst();
    }

    public List<Reservation> getClientReservations(String clientId) {
        return findClientById(clientId)
                .map(Client::getReservationHistory)
                .orElse(new ArrayList<>());
    }
}