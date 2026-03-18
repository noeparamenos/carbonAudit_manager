package com.carbonaudit.dao;

import com.carbonaudit.model.Responsable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO para la gestión del historial de Responsables de Sostenibilidad.
 * Implementa la lógica para manejar periodos de responsabilidad (fechas)
 * y la composición con Departamento y Empleado.
 */
public class ResponsableDAO implements DAO<Responsable, Integer> {

    private final DepartamentoDAO departamentoDAO = new DepartamentoDAO();
    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();

    /**
     * Registra un nuevo responsable en la BD.
     * @param responsable objeto con la vinculación empleado-departamento.
     * @return objeto con el id_asignacion generado.
     */
    @Override
    public Responsable create(Responsable responsable) {
        // Integridad: validación de valores NOT NULL
        validarResponsable(responsable);
        // INSERTAR
        String sql = "INSERT INTO RESPONSABLE (fecha_inicio, fecha_fin, id_dept, id_empleado) VALUES (?, ?, ?, ?) RETURNING id_asignacion";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Conversión de LocalDate a java.sql.Date para JDBC
            pstmt.setDate(1, Date.valueOf(responsable.getFechaInicio()));

            // La fecha de fin puede ser NULL si el responsable aun sigue activo
            if (responsable.getFechaFin() != null) {
                pstmt.setDate(2, Date.valueOf(responsable.getFechaFin()));
            } else {
                pstmt.setNull(2, Types.DATE);
            }

            pstmt.setInt(3, responsable.getDepartamento().getIdDepartamento());
            pstmt.setInt(4, responsable.getEncargado().getIdEmpleado());

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                responsable.setIdAsignacion(rs.getInt(1));
            }
        } catch (SQLException e) {
            // Si se intenta insertar otro responsable con fecha fin NULL
            e.printStackTrace();
        }
        return responsable;
    }

    /**
     * Valida las restricciones de integridad de la tabla Responsable
     * @param responsable a validar
     */
    private void validarResponsable(Responsable responsable) {
        if (responsable.getFechaInicio() == null) {
            throw new IllegalArgumentException("La fecha de inicio es obligatoria (NOT NULL).");
        }
        if (responsable.getDepartamento() == null || responsable.getEncargado() == null) {
            throw new IllegalArgumentException("Un registro de responsabilidad necesita un Departamento y un Empleado.");
        }
    }

    /**
     * Mapea el registro recuperando los objetos completos.
     * @param rs registro recuperado de la BD
     * @return Objeto Responsable Mapeado
     */
    private Responsable mapResultSetToResponsable(ResultSet rs) throws SQLException {
        Responsable res = new Responsable();
        res.setIdAsignacion(rs.getInt("id_asignacion"));

        // Conversión de java.sql.Date a LocalDate
        res.setFechaInicio(rs.getDate("fecha_inicio").toLocalDate());

        Date fechaFinSql = rs.getDate("fecha_fin");
        if (!rs.wasNull()) {
            res.setFechaFin(fechaFinSql.toLocalDate());
        }

        // COMPOSICIÓN:
        int idDept = rs.getInt("id_dept"); // FK del departamento
        departamentoDAO.findById(idDept).ifPresent(res::setDepartamento);

        int idEmp = rs.getInt("id_empleado"); // FK del empleado responsable
        empleadoDAO.findById(idEmp).ifPresent(res::setEncargado);

        return res;
    }

    /**
     * Recupera el registro del historico por id
     * @param id del historico a recuperar
     * @return el Objeto Responsable recuperado si existe
     */
    @Override
    public Optional<Responsable> findById(Integer id) {
        String sql = "SELECT * FROM RESPONSABLE WHERE id_asignacion = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToResponsable(rs)); // Responsable con el id indicado
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty(); // NO hay ningún historico con este id
    }

    /**
     * Recupera el historico completo de responsables de departamento ordenado por fecha de inicio
     * @return la lista del historico de responsables de departamentos
     */
    @Override
    public List<Responsable> findAll() {
        List<Responsable> lista = new ArrayList<>();
        String sql = "SELECT * FROM RESPONSABLE ORDER BY fecha_init DESC";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapResultSetToResponsable(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista; // Historico completo de responsables
    }

    /**
     * Actualiza un registro (útil para poner la fecha_fin cuando un responsable cesa)
     * @param responsable con los datos actualizados
     */
    @Override
    public void update(Responsable responsable) {
        //RESTRICCIONES DE INTEGRIDAD
        validarResponsable(responsable);
        //UPDATE
        String sql = "UPDATE RESPONSABLE SET fecha_inicio = ?, fecha_fin = ?, id_dept = ?, id_empleado = ? WHERE id_asignacion = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(responsable.getFechaInicio()));

            if (responsable.getFechaFin() != null) {
                pstmt.setDate(2, Date.valueOf(responsable.getFechaFin()));
            } else {
                pstmt.setNull(2, Types.DATE); // El responsable sigue activo
            }

            pstmt.setInt(3, responsable.getDepartamento().getIdDepartamento());
            pstmt.setInt(4, responsable.getEncargado().getIdEmpleado());
            pstmt.setInt(5, responsable.getIdAsignacion());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Borra un registro de la Tabla Responsable
     * @param id del registro del historial a borrar
     */
    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM RESPONSABLE WHERE id_asignacion = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}