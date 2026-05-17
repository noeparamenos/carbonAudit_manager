package com.carbonaudit.service;

import com.carbonaudit.dao.DireccionDAO;
import com.carbonaudit.dao.EmpresaDAO;
import com.carbonaudit.model.Direccion;
import com.carbonaudit.model.Empresa;

import java.util.List;

public class ServicioGestionEmpresa {

    private final EmpresaDAO   empresaDAO;
    private final DireccionDAO direccionDAO;

    public ServicioGestionEmpresa() {
        this.empresaDAO   = new EmpresaDAO();
        this.direccionDAO = new DireccionDAO();
    }

    // =========== CRUD EMPRESA ==============

    public List<Empresa> getEmpresas()              { return empresaDAO.findAll(); }
    public void crearEmpresa(Empresa empresa)        { empresaDAO.create(empresa); }
    public void actualizarEmpresa(Empresa empresa)   { empresaDAO.update(empresa); }
    public void eliminarEmpresa(int idEmpresa)       { empresaDAO.delete(idEmpresa); }

    // =========== PERSISTENCIA COORDENADAS ==============

    /** Persiste las coordenadas geocodificadas de una dirección de empresa o departamento. */
    public void persistirCoordenadas(Direccion dir) { direccionDAO.update(dir); }
}