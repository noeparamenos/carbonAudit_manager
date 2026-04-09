package com.carbonaudit.dao;

import com.carbonaudit.model.CommutingEmpleado;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO para la gestión de desplazamientos mensuales de empleados.
 * Maneja una clave primaria compuesta (id_empleado, mes, anio). por lo que no puede implementar la interfaz
 */
public class CommutingEmpleadoDAO {

    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();
    private final FactorEmisionDAO factorEmisionDAO = new FactorEmisionDAO();

    /**
     * Registra un nuevo registro de desplazamiento mensual.
     * @
     */
    public void create(CommutingEmpleado commuting) {
        // 1. VALIDACIONES (NOT NULL y CHECKs)
        validarCommuting(commuting);

        String sql = "INSERT INTO COMMUTING_EMPLEADO (id_empleado, id_factor, distancia_diaria_km, dias_presenciales_mes, mes, anio) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, commuting.getEmpleado().getIdEmpleado());
            pstmt.setInt(2, commuting.getMedioTransporte().getIdFactor());
            pstmt.setObject(3, commuting.getDistanciaDiariaKm(), java.sql.Types.DECIMAL);
            pstmt.setInt(4, commuting.getDiasPresencialesMes());
            pstmt.setInt(5, commuting.getMes());
            pstmt.setInt(6, commuting.getAnio());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            // Si ya existe el registro para ese mes/año/empleado, saltará la restricción PK compuesta
            e.printStackTrace();
        }
    }

    /**
     * Valida todas las restricciones NOT NULL y CHECK
     * @param c
     */
    private void validarCommuting(CommutingEmpleado c) {
        // FK y dependencias NOT NULL
        if (c.getEmpleado() == null || c.getEmpleado().getIdEmpleado() == 0) {
            throw new IllegalArgumentException("El empleado es obligatorio y debe tener un ID válido.");
        }
        if (c.getMedioTransporte() == null || c.getMedioTransporte().getIdFactor() == 0) {
            throw new IllegalArgumentException("El medio de transporte es obligatorio.");
        }

        // Lógica de BD
        if (c.getDistanciaDiariaKm() == null || c.getDistanciaDiariaKm().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La distancia diaria es obligatoria y no puede ser negativa.");
        }
        if (c.getDiasPresencialesMes() < 0 || c.getDiasPresencialesMes() > 31) {
            throw new IllegalArgumentException("Los días presenciales deben estar entre 0 y 31.");
        }

        // CHECKs de la tabla
        if (c.getMes() < 1 || c.getMes() > 12) {
            throw new IllegalArgumentException("Mes no válido (1-12).");
        }
        if (c.getAnio() < 1950) {
            throw new IllegalArgumentException("Año no válido (>= 1950).");
        }
    }

    /**
     * Busca un registro específico por su clave compuesta. (empleado, mes y año)
     * @param idEmpleado que realizo el desplazamiento
     * @param anio en el que realizo el desplazamiento
     * @param mes en el que realizo el desplazamiento
     * @return Un objeto CommutingEmpleado con todos los datos del desplazamiento
     */
    public Optional<CommutingEmpleado> findById(int idEmpleado, int mes, int anio) {
        String sql = "SELECT * FROM COMMUTING_EMPLEADO WHERE id_empleado = ? AND mes = ? AND anio = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idEmpleado);
            pstmt.setInt(2, mes);
            pstmt.setInt(3, anio);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToCommuting(rs)); // Desplazamiento encontrado
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty(); // No hay ningun registro de este empleado para ese mes y año
    }

    /**
     * Mapea el registro de un desplazamiento a un Objeto CommutingEmpleado
     * @param rs registro del desplazamiento a mapear
     * @return un Objeto CommutingEmpleado con todos los datos del desplazamiento si existe
     * @throws SQLException que se maneja en la llamada a este metodo
     */
    private CommutingEmpleado mapResultSetToCommuting(ResultSet rs) throws SQLException {
        CommutingEmpleado c = new CommutingEmpleado();
        c.setDistanciaDiariaKm(rs.getBigDecimal("distancia_diaria_km"));
        c.setDiasPresencialesMes(rs.getInt("dias_presenciales_mes"));
        c.setMes(rs.getInt("mes"));
        c.setAnio(rs.getInt("anio"));

        // Reconstrucción de objetos vinculados (composicion)
        int idEmp = rs.getInt("id_empleado");
        empleadoDAO.findById(idEmp).ifPresent(c::setEmpleado);

        int idFactor = rs.getInt("id_factor");
        factorEmisionDAO.findById(idFactor).ifPresent(c::setMedioTransporte);

        return c;
    }

    /**
     * Recupera todos los registros de desplazamiento.
     * @return la lista de todos los desplazamientos registrados por todos los empleados oredenador por fecha
     */
    public List<CommutingEmpleado> findAll() {
        List<CommutingEmpleado> lista = new ArrayList<>();
        String sql = "SELECT * FROM COMMUTING_EMPLEADO ORDER BY anio DESC, mes DESC";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapResultSetToCommuting(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Actualiza un registro existente (basado en la PK compuesta).
     * @param commuting desplazamiento con los datos actualizados
     */
    public void update(CommutingEmpleado commuting) {
        // Validar restricciones
        validarCommuting(commuting);
        // UPDATE
        String sql = "UPDATE COMMUTING_EMPLEADO SET id_factor = ?, distancia_diaria_km = ?, dias_presenciales_mes = ? " +
                "WHERE id_empleado = ? AND mes = ? AND anio = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, commuting.getMedioTransporte().getIdFactor());
            pstmt.setBigDecimal(2, commuting.getDistanciaDiariaKm());
            pstmt.setInt(3, commuting.getDiasPresencialesMes());
            pstmt.setInt(4, commuting.getEmpleado().getIdEmpleado());
            pstmt.setInt(5, commuting.getMes());
            pstmt.setInt(6, commuting.getAnio());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Elimina un registro de desplazamiento de la BD
     * @param idEmpleado que se desplazó
     * @param mes en el que tubo lugar el desplazamiento
     * @param anio en el que tubo lugar el desplazamiento
     */
    public void delete(int idEmpleado, int mes, int anio) {
        String sql = "DELETE FROM COMMUTING_EMPLEADO WHERE id_empleado = ? AND mes = ? AND anio = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idEmpleado);
            pstmt.setInt(2, mes);
            pstmt.setInt(3, anio);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Recupera todos los registros de commuting de empleados de un departamento para un mes específico
     *
     * @param idDepartamento ID del departamento
     * @param mes           Mes del período (1-12)
     * @param anio          Año del período
     * @return Lista de commutings de empleados del departamento y el mes indicado
     */
    public List<CommutingEmpleado> getCommutingsDepartamentoMes(int idDepartamento, int mes, int anio) {
        List<CommutingEmpleado> lista = new ArrayList<>();
        // JOIN con EMPLEADO para filtrar solo los empleados pertenecientes al departamento
        String sql = "SELECT ce.* FROM COMMUTING_EMPLEADO ce " +
                     "JOIN EMPLEADO e ON ce.id_empleado = e.id_empleado " +
                     "WHERE e.id_dept = ? AND ce.mes = ? AND ce.anio = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idDepartamento);
            pstmt.setInt(2, mes);
            pstmt.setInt(3, anio);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                lista.add(mapResultSetToCommuting(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}