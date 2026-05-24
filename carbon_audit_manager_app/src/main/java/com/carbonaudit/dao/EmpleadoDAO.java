package com.carbonaudit.dao;

import com.carbonaudit.model.Empleado;
import com.carbonaudit.model.Direccion;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmpleadoDAO implements DAO<Empleado, Integer> {

    private final DepartamentoDAO departamentoDAO = new DepartamentoDAO();
    private final DireccionDAO direccionDAO = new DireccionDAO();
    private final FactorEmisionDAO factorEmisionDAO = new FactorEmisionDAO();

    /**
     * Inserta un registro en la tabla Empleado
     * @param empleado con los datos a persistir
     * @return Objeto Empleado con el ID actualizado al asignado por la BD
     */
    @Override
    public Empleado create(Empleado empleado) {
        // Creamos una nueva direccion si se le ha asignado una nueva
        if (empleado.getDireccion().getIdDireccion() == 0) {
            Direccion nuevaDir = direccionDAO.create(empleado.getDireccion());
            empleado.setDireccion(nuevaDir);
        }

        // INSERCION del empleado
        String sql = "INSERT INTO EMPLEADO (nombre, fecha_alta, distancia_trabajo, medio_transporte, dias_presenciales, id_direccion, id_dept) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id_empleado";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, empleado.getNombre());
            pstmt.setDate(2, Date.valueOf(empleado.getFechaAlta()));
            if (empleado.getDistanciaTrabajo() != null) {
                pstmt.setBigDecimal(3, empleado.getDistanciaTrabajo());
            } else {
                pstmt.setNull(3, Types.DECIMAL);
            }
            pstmt.setInt(4, empleado.getMedioTransporte().getIdFactor());
            pstmt.setInt(5, empleado.getDiasPresenciales());
            pstmt.setInt(6, empleado.getDireccion().getIdDireccion());
            pstmt.setInt(7, empleado.getDepartamento().getIdDepartamento());

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                empleado.setIdEmpleado(rs.getInt(1)); // Recuperación del ID autogenerado
            }
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState()))
                throw new IllegalArgumentException("Ya existe un empleado con esos datos.");
            throw new RuntimeException("Error al crear el empleado.", e);
        }
        return empleado;
    }

    /**
     * Mapea un registro recuperado de la BD a un Objeto Empleado
     * @param rs registro de un empleado recuperado de la BD
     * @return objeto Empleado mapeado desde el registro de la BD
     * @throws SQLException
     */
    private Empleado mapResultSetToEmpleado(ResultSet rs) throws SQLException {
        Empleado emp = new Empleado();
        emp.setIdEmpleado(rs.getInt("id_empleado"));
        emp.setNombre(rs.getString("nombre"));
        emp.setFechaAlta(rs.getDate("fecha_alta").toLocalDate());
        Date fechaBajaSql = rs.getDate("fecha_baja");
        if (!rs.wasNull()) emp.setFechaBaja(fechaBajaSql.toLocalDate());
        emp.setDistanciaTrabajo(rs.getBigDecimal("distancia_trabajo"));
        emp.setDiasPresenciales(rs.getInt("dias_presenciales"));

        // Dependencias (composicion)
        int idDep = rs.getInt("id_dept");
        // Si existe el departamento se asigna
        departamentoDAO.findById(idDep).ifPresent(emp::setDepartamento);

        int idDir = rs.getInt("id_direccion");
        // Si existe la dirección se assigna
        direccionDAO.findById(idDir).ifPresent(emp::setDireccion);

        int idFactor = rs.getInt("medio_transporte");
        factorEmisionDAO.findById(idFactor).ifPresent(emp::setMedioTransporte);

        return emp; // Objeto mapeado
    }

    /**
     * Actualiza los datos de un Empleado persitido en la BD
     * @param empleado con los datos actualizados
     */
    @Override
    public void update(Empleado empleado) {
        // Al actualizar, siempre refrescamos los datos de la dirección en su tabla
        if (empleado.getDireccion() != null) {
            direccionDAO.update(empleado.getDireccion());
        }

        String sql = "UPDATE EMPLEADO SET nombre=?, fecha_alta=?, distancia_trabajo=?, medio_transporte=?, dias_presenciales=?, id_direccion=?, id_dept=? WHERE id_empleado=?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, empleado.getNombre());
            pstmt.setDate(2, Date.valueOf(empleado.getFechaAlta()));
            pstmt.setObject(3, empleado.getDistanciaTrabajo(), java.sql.Types.DECIMAL);
            pstmt.setInt(4, empleado.getMedioTransporte().getIdFactor());
            pstmt.setInt(5, empleado.getDiasPresenciales());
            pstmt.setInt(6, empleado.getDireccion().getIdDireccion());
            pstmt.setInt(7, empleado.getDepartamento().getIdDepartamento());
            pstmt.setInt(8, empleado.getIdEmpleado());

            pstmt.executeUpdate(); // Actualización de datos
        } catch (SQLException e) { // Atrapa el error que lanza el mapeado
            e.printStackTrace();
        }
    }

    /**
     * Recupera una Empleado de la BD y lo mapea
     * @param id del empleado que queremos recuperar
     * @return Un Objeto Empleado si existe en la BD con el ID indicado
     */
    @Override
    public Optional<Empleado> findById(Integer id) {
        String sql = "SELECT * FROM EMPLEADO WHERE id_empleado = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return Optional.of(mapResultSetToEmpleado(rs)); // Empleado mapeado
        } catch (SQLException e) { e.printStackTrace(); }

        return Optional.empty(); // No hay un empleado con ese ID
    }

    /**
     * Recupera todos los Empleados de la BD
     * @return la lista de todos los Emplados persistidos
     */
    @Override
    public List<Empleado> findAll() {
        List<Empleado> lista = new ArrayList<>();
        String sql = "SELECT * FROM EMPLEADO";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) lista.add(mapResultSetToEmpleado(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return lista; // de objetos Emplado mapeados
    }

    /**
     * Cuenta cuántos empleados pertenecen a un departamento concreto.
     * Se usa en UI (PA2) para mostrar la columna "Nº empleados" de la tabla.
     *
     * @param idDepartamento ID del departamento
     * @return Número de empleados en ese departamento
     */
    public int countByDepartamento(int idDepartamento) {
        String sql = "SELECT COUNT(*) FROM EMPLEADO WHERE id_dept = ? AND fecha_baja IS NULL";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idDepartamento);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Borra un empleado de la BD.
     * Solo usar cuando el empleado no tenga historial (commuting, responsable).
     * En la mayoría de casos usar darDeBaja() en su lugar.
     */
    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM EMPLEADO WHERE id_empleado = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    /**
     * Devuelve los empleados activos (fecha_baja IS NULL) de un departamento.
     *
     * @param idDepartamento ID del departamento
     * @return lista de empleados activos ordenada por nombre
     */
    public List<Empleado> findAllByDepartamento(int idDepartamento) {
        List<Empleado> lista = new ArrayList<>();
        String sql = "SELECT * FROM EMPLEADO WHERE id_dept = ? AND fecha_baja IS NULL ORDER BY nombre";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idDepartamento);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) lista.add(mapResultSetToEmpleado(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    /**
     * Marca un empleado como dado de baja.
     * Soft Delete: Sus registros de commuting y responsabilidad se conservan para auditoría.
     *
     * @param idEmpleado ID del empleado que causa baja
     * @param fechaBaja  fecha efectiva de la baja
     */
    public void darDeBaja(int idEmpleado, LocalDate fechaBaja) {
        String sql = "UPDATE EMPLEADO SET fecha_baja = ? WHERE id_empleado = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(fechaBaja));
            pstmt.setInt(2, idEmpleado);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}