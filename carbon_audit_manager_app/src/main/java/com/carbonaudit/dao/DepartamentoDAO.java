package com.carbonaudit.dao;

import com.carbonaudit.model.Departamento;
import com.carbonaudit.model.Direccion;
import com.carbonaudit.model.Empresa;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO para la gestión de Departamentos.
 * Maneja una doble composición: cada departamento debe tener obligatoriamente
 * una Empresa y una Dirección asociadas.
 */
public class DepartamentoDAO implements DAO<Departamento, Integer> {

    // DAOs  para resolver la composición
    private final DireccionDAO direccionDAO = new DireccionDAO();
    private final EmpresaDAO empresaDAO = new EmpresaDAO();

    /**
     * Inserta un nuevo departamento.
     *
     * @param departamento Objeto a persistir.
     * @return El objeto con el ID generado por la BD.
     */
    @Override
    public Departamento create(Departamento departamento) {
        // 1. VALIDACIONES DE INTEGRIDAD
        validarDepartamento(departamento);
        // 2. GESTIÓN DE DIRECCIÓN EN CASCADA
        if (departamento.getDireccion().getIdDireccion() == 0) {
            Direccion nuevaDir = direccionDAO.create(departamento.getDireccion());
            departamento.setDireccion(nuevaDir);
        }

        // 3. INSERCIÓN (Incluyendo descripcion e incluir_alcance3)
        String sql = "INSERT INTO DEPARTAMENTO (nombre, descripcion, incluir_alcance3, id_empresa, id_direccion) VALUES (?, ?, ?, ?, ?) RETURNING id_departamento";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, departamento.getNombre());
            pstmt.setString(2, departamento.getDescripcion()); // Campo TEXT
            pstmt.setBoolean(3, departamento.isIncluirAlcance3()); // Campo BOOLEAN
            pstmt.setInt(4, departamento.getEmpresa().getIdEmpresa());
            pstmt.setInt(5, departamento.getDireccion().getIdDireccion());

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                departamento.setIdDepartamento(rs.getInt(1));
            }
        } catch (SQLException e) {
            // Manejo de UNIQUE (id_empresa, nombre)
            e.printStackTrace();
        }
        return departamento;
    }

    /**
     * Valida las restricciones de integridad en la tabla departamento
     * @param departamento
     */
    private void validarDepartamento(Departamento departamento) {
        if (departamento.getNombre() == null || departamento.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del departamento es obligatorio.");
        }
        if (departamento.getEmpresa() == null || departamento.getEmpresa().getIdEmpresa() == 0) {
            throw new IllegalArgumentException("El departamento debe estar vinculado a una empresa válida.");
        }
        if (departamento.getDireccion() == null) {
            throw new IllegalArgumentException("La ubicación física del departamento es obligatoria (NOT NULL).");
        }

    }

    /**
     * Recupera un departamento y reconstruye sus objetos Empresa y Dirección.
     * @param id PK del departamento a buscar
     * @return El Objeto departamento mapeado de la BD si existe. Sino un Null
     */
    @Override
    public Optional<Departamento> findById(Integer id) {
        String sql = "SELECT * FROM DEPARTAMENTO WHERE id_departamento = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToDepartamento(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty(); // No existe el departamento con el ID indicado
    }

    /**
     * Mapea el registro de la BD al objeto Departamento.
     * Resuelve la doble composición mediante los DAOs correspondientes.
     * @param rs Resultado de una consulta para obtener un registro de tipo Departamento en la BD
     * @return Un objeto Departamento mapeado desde la BD
     */
    private Departamento mapResultSetToDepartamento(ResultSet rs) throws SQLException {
        Departamento d = new Departamento();
        d.setIdDepartamento(rs.getInt("id_departamento"));
        d.setNombre(rs.getString("nombre"));
        d.setDescripcion(rs.getString("descripcion"));
        d.setIncluirAlcance3(rs.getBoolean("incluir_alcance3"));

        // Composición de Empresa
        int idEmpresa = rs.getInt("id_empresa");
        empresaDAO.findById(idEmpresa).ifPresent(d::setEmpresa);

        // Composición de Dirección
        int idDireccion = rs.getInt("id_direccion");
        direccionDAO.findById(idDireccion).ifPresent(d::setDireccion);

        return d;
    }

    /**
     * Recupera todos los departamentos de la BD y los devuelve Mapeados en una Lista
     * @return Una lista con todos los departamentos de la BD
     */
    @Override
    public List<Departamento> findAll() {
        List<Departamento> lista = new ArrayList<>();
        String sql = "SELECT * FROM DEPARTAMENTO";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapResultSetToDepartamento(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista; // Lista de Objetos Departamento mapeados desde la BD
    }

    /**
     * Actualiza el departamento y su dirección asociada.
     * @param departamento del que se quieren actualizar los datos con los datos actualizados
     */
    @Override
    public void update(Departamento departamento) {
        //VALIDAR RESTRICCIONES
        validarDepartamento(departamento);
        // Actualizamos dirección primero
        if (departamento.getDireccion() != null) {
            direccionDAO.update(departamento.getDireccion());
        }

        String sql = "UPDATE DEPARTAMENTO SET nombre = ?, descripcion = ?, incluir_alcance3 = ?, id_empresa = ?, id_direccion = ? WHERE id_departamento = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, departamento.getNombre());
            pstmt.setString(2, departamento.getDescripcion());
            pstmt.setBoolean(3, departamento.isIncluirAlcance3());
            pstmt.setInt(4, departamento.getEmpresa().getIdEmpresa());
            pstmt.setInt(5, departamento.getDireccion().getIdDireccion());
            pstmt.setInt(6, departamento.getIdDepartamento());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Borra un Departamento de la BD
     * @param id del departamento a borrar
     */
    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM DEPARTAMENTO WHERE id_departamento = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}