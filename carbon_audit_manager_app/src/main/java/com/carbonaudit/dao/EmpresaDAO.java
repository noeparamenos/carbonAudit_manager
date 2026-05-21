package com.carbonaudit.dao;

import com.carbonaudit.model.Empresa;
import com.carbonaudit.model.Direccion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO para la gestión de Empresas.
 * Implementa la composición cargando el objeto Direccion asociado.
 */
public class EmpresaDAO implements DAO<Empresa, Integer> {

    // DAO de direcciones para la composición
    private final DireccionDAO direccionDAO = new DireccionDAO();

    /**
     * Introduce una nueva empresa en la BD
     *
     * @param empresa a introducir en la BD
     * @return el objeto empresa con el ID asignado por la BD
     */
    @Override
    public Empresa create(Empresa empresa) {
        // DIRECCION (si no existe se crea)
        if (empresa.getDireccion().getIdDireccion() == 0) {
            Direccion dirGuardada = direccionDAO.create(empresa.getDireccion());
            empresa.setDireccion(dirGuardada);
        }

        // INSERCION
        String sql = "INSERT INTO EMPRESA (nombre_social, cif, telefono, email, sector, id_direccion) VALUES (?, ?, ?, ?, ?, ?) RETURNING id_empresa";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, empresa.getNombreSocial());
            pstmt.setString(2, empresa.getCif());
            pstmt.setString(3, empresa.getTelefono());
            pstmt.setString(4, empresa.getEmail());
            pstmt.setString(5, empresa.getSector());
            // FK NOT NULL (composicion)
            pstmt.setInt(6, empresa.getDireccion().getIdDireccion());

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                empresa.setIdEmpresa(rs.getInt(1));// Guardamos el id asignado por la BD en el objeto
            }
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState()))
                throw new IllegalArgumentException("Ya existe una empresa con ese CIF o nombre social.");
            throw new RuntimeException("Error al crear la empresa.", e);
        }
        return empresa;
    }

    /**
     * Busca una empresa concreta por su id (PK de la BD)
     *
     * @param id PK de la empresa a buscar
     * @return Objeto Empresa si existe.
     */
    @Override
    public Optional<Empresa> findById(Integer id) {
        String sql = "SELECT * FROM EMPRESA WHERE id_empresa = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) { //Fila encontrada
                //Mapea la fila y envuelve el objeto en un Optional
                return Optional.of(mapResultSetToEmpresa(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty(); // empresa no encontrada
    }

    /**
     * Mapeo del registro de una Empresa en la BD a un Objeto Empresa
     * con Composición de Direccion.
     *
     * @param rs El resultado de una consulta que devuelve un Registro de la tabla Empresa
     * @return El objeto empresa con los datos extradidos de la BD
     */
    private Empresa mapResultSetToEmpresa(ResultSet rs) throws SQLException {
        Empresa e = new Empresa();
        e.setIdEmpresa(rs.getInt("id_empresa"));
        e.setNombreSocial(rs.getString("nombre_social"));
        e.setCif(rs.getString("cif"));
        e.setTelefono(rs.getString("telefono"));
        e.setEmail(rs.getString("email"));
        e.setSector(rs.getString("sector"));

        // COMPOSICIÓN: resuelve la FK cargando el objeto Direccion completo
        int idDir = rs.getInt("id_direccion");
        direccionDAO.findById(idDir).ifPresent(e::setDireccion);
        return e;
    }

    /**
     * Recorre la BD y recupera todas las empresas
     *
     * @return Una Lista con todas las empresas de la BD
     */
    @Override
    public List<Empresa> findAll() {
        List<Empresa> empresas = new ArrayList<>();
        String sql = "SELECT * FROM EMPRESA";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                empresas.add(mapResultSetToEmpresa(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return empresas;
    }


    /**
     * Actualiza los datos de una Empresa presente en la BD.
     *
     * @param empresa objeto con los datos actualizados (debe tener idEmpresa válido)
     */
    @Override
    public void update(Empresa empresa) {
        // Si se la empresa tiene una direccion
        if (empresa.getDireccion() != null) {
            // Si se la ha asignado una direccion nueva se crea
            if (empresa.getDireccion().getIdDireccion() == 0) {
                direccionDAO.create(empresa.getDireccion());
            } else {
                // Si se ha modificado la direccion
                direccionDAO.update(empresa.getDireccion());
            }
        }

        String sql = "UPDATE EMPRESA SET nombre_social=?, cif=?, telefono=?, email=?, sector=?, id_direccion=? WHERE id_empresa=?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, empresa.getNombreSocial());
            pstmt.setString(2, empresa.getCif());
            pstmt.setString(3, empresa.getTelefono());
            pstmt.setString(4, empresa.getEmail());
            pstmt.setString(5, empresa.getSector());

            // Si la empresa tiene una direccion le asignamos su ID (FK)
            if (empresa.getDireccion() != null) {
                pstmt.setInt(6, empresa.getDireccion().getIdDireccion());
            } else {//Si no tiene direccion guardamos un NULL en la BD
                pstmt.setNull(6, java.sql.Types.INTEGER);
            }

            pstmt.setInt(7, empresa.getIdEmpresa());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Borra una el registro de una empresa en la BD
     *
     * @param id (PK) de la empresa a borrar
     */
    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM EMPRESA WHERE id_empresa = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}