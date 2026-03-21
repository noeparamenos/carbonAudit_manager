package com.carbonaudit.dao;

import com.carbonaudit.model.Empleado;
import com.carbonaudit.model.Direccion;
import java.sql.*;
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
        // Validaciones de integridad (campos NOT NULL de la BD)
        validarEmpleado(empleado);

        // Creamos una nueva direccion si se le ha asignado una nueva
        if (empleado.getDireccion().getIdDireccion() == 0) {
            Direccion nuevaDir = direccionDAO.create(empleado.getDireccion());
            empleado.setDireccion(nuevaDir);
        }

        // INSERCION del empleado
        String sql = "INSERT INTO EMPLEADO (nombre, distancia_trabajo, medio_transporte, dias_presenciales, id_direccion, id_dept) VALUES (?, ?, ?, ?, ?, ?) RETURNING id_empleado";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, empleado.getNombre());
            // distancia_trabajo puede ser NULL (se calcula via API después)
            if (empleado.getDistanciaTrabajo() != null) {
                pstmt.setBigDecimal(2, empleado.getDistanciaTrabajo());
            } else {
                pstmt.setNull(2, Types.DECIMAL);
            }

            // Ya no comprobamos nulos aquí porque la validación inicial y la BD no lo permiten
            pstmt.setInt(3, empleado.getMedioTransporte().getIdFactor());
            pstmt.setInt(4, empleado.getDiasPresenciales());
            pstmt.setInt(5, empleado.getDireccion().getIdDireccion());
            pstmt.setInt(6, empleado.getDepartamento().getIdDepartamento());

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                empleado.setIdEmpleado(rs.getInt(1)); // Recuperación del ID autogenerado
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return empleado;
    }

    private void validarEmpleado(Empleado empleado) {
        if (empleado.getNombre() == null || empleado.getNombre().isEmpty()) {
            throw new IllegalArgumentException("El nombre del empleado es obligatorio.");
        }
        if (empleado.getDireccion() == null) {
            throw new IllegalArgumentException("La dirección de residencia es obligatoria");
        }
        if (empleado.getMedioTransporte() == null) {
            throw new IllegalArgumentException("El medio de transporte es obligatorio para el cálculo de movilidad.");
        }
        if (empleado.getDepartamento() == null || empleado.getDepartamento().getIdDepartamento() == 0) {
            throw new IllegalArgumentException("El departamento es obligatorio.");
        }

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
        // Restricciones de integridad
        validarEmpleado(empleado);
        // Al actualizar, siempre refrescamos los datos de la dirección en su tabla
        if (empleado.getDireccion() != null) {
            direccionDAO.update(empleado.getDireccion());
        }

        String sql = "UPDATE EMPLEADO SET nombre=?, distancia_trabajo=?, medio_transporte=?, dias_presenciales=?, id_direccion=?, id_dept=? WHERE id_empleado=?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, empleado.getNombre());
            pstmt.setObject(2, empleado.getDistanciaTrabajo(), java.sql.Types.DECIMAL);
            pstmt.setInt(3, empleado.getMedioTransporte().getIdFactor());
            pstmt.setInt(4, empleado.getDiasPresenciales());
            pstmt.setInt(5, empleado.getDireccion().getIdDireccion());
            pstmt.setInt(6, empleado.getDepartamento().getIdDepartamento());
            pstmt.setInt(7, empleado.getIdEmpleado()); // PK del empleado a actualziar

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
     * Borra un emplado de la BD
     * @param id del emplado a borrar
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
}