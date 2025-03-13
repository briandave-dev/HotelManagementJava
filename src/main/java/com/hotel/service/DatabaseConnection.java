package com.hotel.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://" + EnvLoader.get("DB_HOST") + ":" + EnvLoader.get("DB_PORT") + "/" + EnvLoader.get("DB_NAME");
    private static final String USER = EnvLoader.get("DB_USER");
    private static final String PASSWORD = EnvLoader.get("DB_PASSWORD");

    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        // Le chargement explicite du pilote est facultatif à partir de JDBC 4.0
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
