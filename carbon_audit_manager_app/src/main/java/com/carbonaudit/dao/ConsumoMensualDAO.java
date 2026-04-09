package com.carbonaudit.dao;

import com.carbonaudit.model.ConsumoMensual;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO para la gestión de Consumos Mensuales.
 * Implementa validaciones estrictas para cumplir con las restricciones NOT NULL
 * y CHECK de la base de datos.
 */
public class ConsumoMensualDAO implements DAO<ConsumoMensual, Integer> {

    private final DepartamentoDAO departamentoDAO = new DepartamentoDAO();
    private final FactorEmisionDAO factorEmisionDAO = new FactorEmisionDAO();

    /**
     * Inserta un registro mensual de consumo
     *
     * @param consumo a persistir en la BD
     * @return El Objeto ConsumoMensual con su ID asignado
     */
    @Override
    public ConsumoMensual create(ConsumoMensual consumo) {
        // 1. VALIDACIONES DE INTEGRIDAD (NOT NULL)
        validarConsumoMensual(consumo);


        // INSERCIÓN
        String sql = "INSERT INTO CONSUMO_MENSUAL (cantidad, mes, anio, id_dept, id_factor) VALUES (?, ?, ?, ?, ?) RETURNING id_consumo";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBigDecimal(1, consumo.getCantidad());
            pstmt.setInt(2, consumo.getMes());
            pstmt.setInt(3, consumo.getAnio());
            pstmt.setInt(4, consumo.getDepartamento().getIdDepartamento());
            pstmt.setInt(5, consumo.getFactorEmision().getIdFactor());

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                consumo.setIdConsumo(rs.getInt(1));
            }
        } catch (SQLException e) {
            //  si violamos el UNIQUE(id_dept, id_factor, mes, anio)
            e.printStackTrace();
        }
        return consumo;
    }

    /**
     * Valida las restricciones de integridad de la tabla ConsumoMensual
     * @param consumo a validar
     */
    private void validarConsumoMensual(ConsumoMensual consumo) {

        if (consumo.getCantidad() == null) {
            throw new IllegalArgumentException("La cantidad de consumo no puede ser nula.");
        }
        if (consumo.getDepartamento() == null || consumo.getDepartamento().getIdDepartamento() == 0) {
            throw new IllegalArgumentException("El departamento es obligatorio y debe ser válido.");
        }

        if (consumo.getFactorEmision() == null || consumo.getFactorEmision().getIdFactor() == 0) {
            throw new IllegalArgumentException("El factor de emisión es obligatorio.");
        }

        // VALIDACIONES DE INTEGRIDAD (CHECK)
        if (consumo.getMes() < 1 || consumo.getMes() > 12) {
            throw new IllegalArgumentException("El mes debe estar entre 1 y 12.");
        }

        if (consumo.getAnio() < 1950) {
            throw new IllegalArgumentException("El año debe ser igual o superior a 1950.");
        }
    }

    /**
     * Reconstruye el consumo con sus dependencias obligatorias.
     *
     * @param rs registro recuperado de la BD
     * @return El Objeto ConsumoMensual mapeado con los datos recuperados
     */
    private ConsumoMensual mapResultSetToConsumo(ResultSet rs) throws SQLException {
        ConsumoMensual c = new ConsumoMensual();
        c.setIdConsumo(rs.getInt("id_consumo"));
        c.setCantidad(rs.getBigDecimal("cantidad"));
        c.setMes(rs.getInt("mes"));
        c.setAnio(rs.getInt("anio"));

        // Mapeo de FK (Al ser NOT NULL, no hace falta comprobar wasNull)
        int idDept = rs.getInt("id_dept");
        departamentoDAO.findById(idDept).ifPresent(c::setDepartamento);

        int idFactor = rs.getInt("id_factor");
        factorEmisionDAO.findById(idFactor).ifPresent(c::setFactorEmision);

        return c;
    }

    /**
     * Actualiza los datos de un consumo registrado previamente en la BD
     *
     * @param consumo con los datos actualizados
     */
    @Override
    public void update(ConsumoMensual consumo) {
        // Volvemos a validar antes de actualizar para mantener la integridad
        validarConsumoMensual(consumo);
        // UPDATE
        String sql = "UPDATE CONSUMO_MENSUAL SET cantidad=?, mes=?, anio=?, id_dept=?, id_factor=? WHERE id_consumo=?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBigDecimal(1, consumo.getCantidad());
            pstmt.setInt(2, consumo.getMes());
            pstmt.setInt(3, consumo.getAnio());
            pstmt.setInt(4, consumo.getDepartamento().getIdDepartamento());
            pstmt.setInt(5, consumo.getFactorEmision().getIdFactor());
            pstmt.setInt(6, consumo.getIdConsumo());

            pstmt.executeUpdate(); // Actualización del registro
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Recupera el registro de un consumo
     *
     * @param id del consumo a recuperar de la BD
     * @return El Objeto ConsumoMensual mapeado de la BD si existía
     */
    @Override
    public Optional<ConsumoMensual> findById(Integer id) {
        String sql = "SELECT * FROM CONSUMO_MENSUAL WHERE id_consumo = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return Optional.of(mapResultSetToConsumo(rs)); // Consumo con el id solicitado
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty(); // NO existe ningun consumo con este id
    }

    /**
     * Recupera el historico con todos los consumos realizados ordenados por fecha
     *
     * @return la lista de consumos realizados ordenados por fecha
     */
    @Override
    public List<ConsumoMensual> findAll() {
        List<ConsumoMensual> consumos = new ArrayList<>();
        String sql = "SELECT * FROM CONSUMO_MENSUAL ORDER BY anio DESC, mes DESC";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) consumos.add(mapResultSetToConsumo(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return consumos;
    }

    /**
     * Borra el registro de un consumo
     *
     * @param id del registro a eliminar
     */
    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM CONSUMO_MENSUAL WHERE id_consumo = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Recupera todos los consumos de un departamento para un período específico (mes y año)
     *
     * @param idDepartamento ID del departamento
     * @param mes           Mes del período (1-12)
     * @param anio          Año del período
     * @return Lista de consumos del departamento en ese período
     */
    public List<ConsumoMensual> getConsumosDepartamentoMes(int idDepartamento, int mes, int anio) {
        List<ConsumoMensual> consumos = new ArrayList<>();
        String sql = "SELECT * FROM CONSUMO_MENSUAL WHERE id_dept = ? AND mes = ? AND anio = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idDepartamento);
            pstmt.setInt(2, mes);
            pstmt.setInt(3, anio);


            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                consumos.add(mapResultSetToConsumo(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return consumos;
    }
}