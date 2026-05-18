package com.carbonaudit.service;

import com.carbonaudit.dao.DireccionDAO;
import com.carbonaudit.dao.EmpresaDAO;
import com.carbonaudit.model.Direccion;
import com.carbonaudit.model.Empresa;
import com.carbonaudit.util.Validador;

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
    public void crearEmpresa(Empresa empresa) {
        Validador.validarCIF(empresa.getCif());
        Validador.validarEmail(empresa.getEmail());
        Validador.validarTelefono(empresa.getTelefono());
        Validador.validarDireccion(empresa.getDireccion());
        empresaDAO.create(empresa);
    }

    public void actualizarEmpresa(Empresa empresa) {
        Validador.validarCIF(empresa.getCif());
        Validador.validarEmail(empresa.getEmail());
        Validador.validarTelefono(empresa.getTelefono());
        Validador.validarDireccion(empresa.getDireccion());
        empresaDAO.update(empresa);
    }
    public void eliminarEmpresa(int idEmpresa)       { empresaDAO.delete(idEmpresa); }

    // =========== PERSISTENCIA COORDENADAS ==============

    /** Persiste las coordenadas geocodificadas de una dirección de empresa o departamento. */
    public void persistirCoordenadas(Direccion dir) { direccionDAO.update(dir); }
}