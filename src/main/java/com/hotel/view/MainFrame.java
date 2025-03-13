package com.hotel.view;

import com.hotel.service.ClientService;
import com.hotel.service.RoomService;
import com.hotel.service.ReservationService;
import com.hotel.service.InvoiceService;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final ClientService clientService;
    private final RoomService roomService;
    private final ReservationService reservationService;
    private final InvoiceService invoiceService;

    private JPanel mainPanel;
    private CardLayout cardLayout;

    public MainFrame() {
        // Initialize services
       
        this.roomService = new RoomService();
        this.clientService = new ClientService(roomService);
        this.reservationService = new ReservationService(roomService, clientService);
        this.invoiceService = new InvoiceService(0.20); // 20% tax rate

        initializeFrame();
        createMenuBar();
        initializeMainPanel();
    }

    private void initializeFrame() {
        setTitle("Hotel Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Create menus
        JMenu clientMenu = new JMenu("Clients");
        JMenu roomMenu = new JMenu("Rooms");
        JMenu reservationMenu = new JMenu("Reservations");
        JMenu invoiceMenu = new JMenu("Invoices");

        // Add menu items
        addMenuItem(clientMenu, "Manage Clients", e -> showPanel("clients"));
        addMenuItem(roomMenu, "Manage Rooms", e -> showPanel("rooms"));
        addMenuItem(reservationMenu, "Manage Reservations", e -> showPanel("reservations"));
        addMenuItem(invoiceMenu, "View Invoices", e -> showPanel("invoices"));

        // Add menus to menu bar
        menuBar.add(clientMenu);
        menuBar.add(roomMenu);
        menuBar.add(reservationMenu);
        menuBar.add(invoiceMenu);

        setJMenuBar(menuBar);
    }

    private void addMenuItem(JMenu menu, String text, java.awt.event.ActionListener listener) {
        JMenuItem menuItem = new JMenuItem(text);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
    }

    private void initializeMainPanel() {
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Create panels for different sections
        mainPanel.add(new ClientPanel(clientService), "clients");
        mainPanel.add(new RoomPanel(roomService), "rooms");
        mainPanel.add(new ReservationPanel(reservationService, invoiceService), "reservations");
        mainPanel.add(new InvoicePanel(invoiceService,reservationService), "invoices");

        // Add welcome panel
        JPanel welcomePanel = createWelcomePanel();
        mainPanel.add(welcomePanel, "welcome");

        add(mainPanel);
        showPanel("welcome");
    }

    private JPanel createWelcomePanel() {
        JPanel welcomePanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome to Hotel Management System", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomePanel.add(welcomeLabel, BorderLayout.CENTER);
        return welcomePanel;
    }

    private void showPanel(String name) {
        cardLayout.show(mainPanel, name);
    }
}