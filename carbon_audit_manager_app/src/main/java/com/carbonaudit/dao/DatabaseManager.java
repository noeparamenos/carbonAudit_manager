package com.carbonaudit.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    private static DatabaseManager instance;

    // Credenciales para el acceso a la BD
    private static final String URL = "jdbc:postgresql://localhost:5432/carbon_audit";
    private static final String USER = "ntcdev";
    private static final String PASSWORD = "Grandvalira2020";


    // Constructor privado
    // Solo habra una sola instancia (Architectura Singleton) que se conseguira a traves del métod
    private DatabaseManager() {
        try {
            // Aseguramos que el driver de PostgreSQL esté cargado en memoria
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Error crítico: Driver JDBC de PostgreSQL no encontrado.");
            e.printStackTrace();
        }
    }

    // Métod sincronizado para obtener la única instancia del manager
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    // Métod que los DAOs llamarán para obtener una conexión a la BD
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}