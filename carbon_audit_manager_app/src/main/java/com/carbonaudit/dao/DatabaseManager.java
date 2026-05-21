package com.carbonaudit.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {

    private static DatabaseManager instance;

    private static final Dotenv dotenv   = Dotenv.load();
    private static final String URL      = dotenv.get("DB_URL");
    private static final String USER     = dotenv.get("DB_USER");
    private static final String PASSWORD = dotenv.get("DB_PASSWORD");

    // Pool de conexiones
    private final HikariDataSource pool;

    /**
     * Constructor privado. Inicializa el pool de conexiones HikariCP con los
     * parámetros de conexión y los límites de tamaño, tiempo de espera y vida útil.
     */
    private DatabaseManager() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(URL);
        config.setUsername(USER);
        config.setPassword(PASSWORD);
        config.setMaximumPoolSize(10); //Maximo de conexiones
        config.setMinimumIdle(2); // Minimo de conexiones abiertas
        config.setConnectionTimeout(30_000);  // máximo esperando una conexión libre del pool
        config.setIdleTimeout(600_000);        // antes de cerrar una conexión ociosa
        config.setMaxLifetime(1_800_000);      // vida máxima de una conexión (evita conexiones zombie)
        this.pool = new HikariDataSource(config);
    }

    /**
     * Devuelve la instancia única del gestor de base de datos (Singleton).
     * {@code synchronized} evita que dos hilos creen instancias simultáneas
     * durante el primer acceso al arrancar la aplicación.
     *
     * @return instancia única de {@code DatabaseManager}
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Obtiene una conexión del pool. El llamador es responsable de cerrarla
     * (preferiblemente con try-with-resources) para devolverla al pool.
     *
     * @return conexión activa a la base de datos
     * @throws SQLException si el pool no puede proporcionar una conexión
     */
    public Connection getConnection() throws SQLException {
        return pool.getConnection();
    }
}