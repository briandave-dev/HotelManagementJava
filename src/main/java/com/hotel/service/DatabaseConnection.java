package com.hotel.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = EnvLoader.get("DB_URL");
    private static final String USER = EnvLoader.get("DB_USER");
    private static final String PASSWORD = EnvLoader.get("DB_PASSWORD");

    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        // Le chargement explicite du pilote est facultatif à partir de JDBC 4.0
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
