package com.carbonaudit.dao;

import com.carbonaudit.model.FactorEmision;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Gestiona las operaciones CRUD sobre la tabla FACTOR_EMISION
 */
public class FactorEmisionDAO implements DAO<FactorEmision, Integer> {

    /**
     * Inserta un nuevo factor en la tabla factor_emision de la BD
     * @param factor de emision a insertar en la BD
     * @return El Objeto factor con ID actualizado (PK en al BD)
     */
    @Override
    public FactorEmision create(FactorEmision factor) {
        // 1. VALIDACIÓN DE RESTRICCIONES (Aseguramos NOT NULL y CHECK)
        validarFactor(factor);

        String sql = "INSERT INTO FACTOR_EMISION (nombre, unidad, valor_factor, alcance) " +
                        "VALUES (?, ?, ?, ?) RETURNING id_factor";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, factor.getNombre());
            pstmt.setString(2, factor.getUnidad());
            pstmt.setBigDecimal(3, factor.getValorFactor()); // Para mayor Precision
            pstmt.setInt(4, factor.getAlcance()); // Alcance (1,2 o 3)

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                factor.setIdFactor(rs.getInt(1)); // Asignamos el ID autogenerado por la BD
            }
        } catch (SQLException e) {
            // Lanza error si se viola la restriccion UNIQUE (nombre, unidad)
            e.printStackTrace();
        }
        return factor; // Objeto con el ID actualizado generado por la BD
    }

    /**
     * Valida las restricciones antes de tocar la BD.
     * @param f factor de emision a validar
     */
    private void validarFactor(FactorEmision f) {
        if (f.getNombre() == null || f.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del factor es obligatorio.");
        }
        if (f.getUnidad() == null || f.getUnidad().trim().isEmpty()) {
            throw new IllegalArgumentException("La unidad (kWh, litros, etc.) es obligatoria.");
        }
        if (f.getValorFactor() == null) {
            throw new IllegalArgumentException("El coeficiente de emisión no puede ser nulo.");
        }
        // Validación del CHECK (alcance IN (1,2,3))
        if (f.getAlcance() < 1 || f.getAlcance() > 3) {
            throw new IllegalArgumentException("Alcance no válido. Debe ser 1 (Directo), 2 (Energía) o 3 (Otros indirectos).");
        }
    }

    /**
     * Recupera un Factor de la BD y lo mapea a un Objeto FactorEmision
     * @param id del Factor a recuperar de la BD
     * @return Objeto FactorEmision Mapeado desde la BD si existe
     */
    @Override
    public Optional<FactorEmision> findById(Integer id) {
        String sql = "SELECT * FROM FACTOR_EMISION WHERE id_factor = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToFactor(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Mapea un registro de la tabla Factor_Emision a un objeto FactorEmision
     * @param rs registro recuperado de la BD
     * @return un objeto FactorEmsion
     */
    private FactorEmision mapResultSetToFactor(ResultSet rs) throws SQLException {
        FactorEmision factor = new FactorEmision();
        factor.setIdFactor(rs.getInt("id_factor"));
        factor.setNombre(rs.getString("nombre"));
        factor.setUnidad(rs.getString("unidad"));
        factor.setValorFactor(rs.getBigDecimal("valor_factor"));
        factor.setAlcance(rs.getInt("alcance"));

        return factor;
    }

    /**
     * Recupera todos los registros de la tabla FACTOR_EMISION y los devuelve mapeados en una lista
     * @return Una lista de objetos FactorEmsion
     */
    @Override
    public List<FactorEmision> findAll() {
        List<FactorEmision> factores = new ArrayList<>();
        String sql = "SELECT * FROM FACTOR_EMISION";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                factores.add(mapResultSetToFactor(rs)); // Mapeado a Objetos
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return factores;
    }

    /**
     * Modifica un registro de la tabla factor de Emsion
     * @param factor de emision a modificar
     */
    @Override
    public void update(FactorEmision factor) {
        // Validacion de restricciones
        validarFactor(factor);
        String sql = "UPDATE FACTOR_EMISION SET nombre = ?, unidad = ?, valor_factor = ?, alcance = ? WHERE id_factor = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, factor.getNombre());
            pstmt.setString(2, factor.getUnidad());
            pstmt.setBigDecimal(3, factor.getValorFactor());
            pstmt.setInt(4, factor.getAlcance());
            pstmt.setInt(5, factor.getIdFactor()); // (PK) en la tabla para realizar el UPDATE

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Elimina un registro de la tabla en la BD
     * @param id (pk) del factor de emisión a eliminar
     */
    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM FACTOR_EMISION WHERE id_factor = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}