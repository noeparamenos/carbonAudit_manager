package com.carbonaudit.dao;

import com.carbonaudit.model.Direccion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO para la gestión de persistencia de la entidad Direccion.
 * Centraliza las operaciones CRUD contra la tabla DIRECCION de PostgreSQL.
 */
public class DireccionDAO implements DAO<Direccion, Integer> {

    /**
     * Inserta un nuevo registro en la tabla DIRECCION de la BD
     * y actualiza el ID del objeto con el ID autogenerado en la BD.
     *
     * @param direccion El objeto Direccion a persistir.
     * @return El mismo objeto Direccion con su idDireccion actualizado.
     */
    @Override
    public Direccion create(Direccion direccion) {
        // VALIDACIÓN DE RESTRICCIONES
        validarDireccion(direccion);

        // INSERCION
        String sql = "INSERT INTO DIRECCION (calle, numero, ciudad, codigo_postal, provincia, latitud, longitud) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id_direccion";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, direccion.getCalle());
            pstmt.setInt(2, direccion.getNumero()); // <-- Nuevo campo añadido
            pstmt.setString(3, direccion.getCiudad());
            pstmt.setString(4, direccion.getCodigoPostal());
            pstmt.setString(5, direccion.getProvincia());

            // Coordenadas opcionales (DECIMAL en BD)
            pstmt.setObject(6, direccion.getLatitud(), java.sql.Types.DECIMAL);
            pstmt.setObject(7, direccion.getLongitud(), java.sql.Types.DECIMAL);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                direccion.setIdDireccion(rs.getInt(1)); // Recuperar el id autogenerado
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return direccion;
    }

    /**
     * Valida las restricciones de integridad de la BD (NOT NULL)
     * @param d direccion a analizar
     */
    private void validarDireccion(Direccion d) {
        if (d.getCalle() == null || d.getCalle().trim().isEmpty())
            throw new IllegalArgumentException("La calle es obligatoria.");
        if (d.getNumero() <= 0)
            throw new IllegalArgumentException("El número debe ser un valor positivo.");
        if (d.getCiudad() == null || d.getCiudad().trim().isEmpty())
            throw new IllegalArgumentException("La ciudad es obligatoria.");
        if (d.getCodigoPostal() == null || d.getCodigoPostal().trim().isEmpty())
            throw new IllegalArgumentException("El código postal es obligatorio para el geocoding.");
    }

    /**
     * Busca una dirección específica por su clave primaria.
     *
     * @param id El identificador único de la dirección.
     * @return Un Optional que contiene el Objeto Direccion si existe, o vacío si no.
     */
    @Override
    public Optional<Direccion> findById(Integer id) {
        String sql = "SELECT * FROM DIRECCION WHERE id_direccion = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) { // Si hay una dirección con el ID pasado, la devuelve
                return Optional.of(mapResultSetToDireccion(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Mapea las filas devueltas por la BD a objetos Java.
     * Mantiene la precisión técnica usando BigDecimal para coordenadas.
     * * @param rs Conjunto de filas devueltas por la bd.
     * @return Un Objeto Direccion con todos los datos de la fila en la BD.
     */
    private Direccion mapResultSetToDireccion(ResultSet rs) throws SQLException {
        Direccion d = new Direccion();
        d.setIdDireccion(rs.getInt("id_direccion"));
        d.setCalle(rs.getString("calle"));
        d.setNumero(rs.getInt("numero")); // <-- Recuperamos el número
        d.setCiudad(rs.getString("ciudad"));
        d.setCodigoPostal(rs.getString("codigo_postal"));
        d.setProvincia(rs.getString("provincia"));
        d.setLatitud(rs.getBigDecimal("latitud"));
        d.setLongitud(rs.getBigDecimal("longitud"));
        return d;
    }

    /**
     * Accede a todas las direcciones de la BD.
     * @return una lista con todas las direcciones registradas en la BD.
     */
    @Override
    public List<Direccion> findAll() {
        List<Direccion> lista = new ArrayList<>();
        String sql = "SELECT * FROM DIRECCION";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapResultSetToDireccion(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Actualiza los datos de una dirección guardada previamente en la BD.
     * @param direccion Objeto con los datos actualizados.
     */
    @Override
    public void update(Direccion direccion) {
        validarDireccion(direccion);

        String sql = "UPDATE DIRECCION SET calle=?, numero=?, ciudad=?, codigo_postal=?, provincia=?, latitud=?, longitud=? WHERE id_direccion=?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, direccion.getCalle());
            pstmt.setInt(2, direccion.getNumero());
            pstmt.setString(3, direccion.getCiudad());
            pstmt.setString(4, direccion.getCodigoPostal());
            pstmt.setString(5, direccion.getProvincia());
            pstmt.setObject(6, direccion.getLatitud(), java.sql.Types.DECIMAL);
            pstmt.setObject(7, direccion.getLongitud(), java.sql.Types.DECIMAL);
            pstmt.setInt(8, direccion.getIdDireccion());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Borra una dirección guardada en la BD.
     * @param id PK de la dirección a borrar.
     */
    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM DIRECCION WHERE id_direccion = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}