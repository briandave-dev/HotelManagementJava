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
        setSize(900, 700);
        setLocationRelativeTo(null);
        
        // Set a modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Set a custom icon if available
        // setIconImage(new ImageIcon("path/to/icon.png").getImage());
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
        
        // Create a gradient panel for background
        JPanel gradientPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth(), h = getHeight();
                Color color1 = new Color(66, 139, 202);
                Color color2 = new Color(51, 122, 183);
                GradientPaint gp = new GradientPaint(0, 0, color1, w, h, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        
        JLabel welcomeLabel = new JLabel("Welcome to Hotel Management System", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeLabel.setForeground(Color.WHITE);
        
        JLabel subLabel = new JLabel("Select an option from the menu above to get started", SwingConstants.CENTER);
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subLabel.setForeground(Color.WHITE);
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        textPanel.setOpaque(false);
        textPanel.add(welcomeLabel);
        textPanel.add(subLabel);
        
        gradientPanel.setLayout(new GridBagLayout());
        gradientPanel.add(textPanel);
        
        welcomePanel.add(gradientPanel, BorderLayout.CENTER);
        return welcomePanel;
    }

    private void showPanel(String name) {
        cardLayout.show(mainPanel, name);
    }
}