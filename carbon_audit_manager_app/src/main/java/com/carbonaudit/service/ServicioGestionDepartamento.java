package com.carbonaudit.service;

import com.carbonaudit.dao.DepartamentoDAO;
import com.carbonaudit.model.Departamento;

import java.util.List;

public class ServicioGestionDepartamento {

    private final DepartamentoDAO departamentoDAO;

    public ServicioGestionDepartamento() {
        this.departamentoDAO = new DepartamentoDAO();
    }

    // =========== CRUD DEPARTAMENTO ==============

    public List<Departamento> getDepartamentosEmpresa(int idEmpresa) {
        return departamentoDAO.findAllByEmpresa(idEmpresa);
    }

    public void crearDepartamento(Departamento departamento)    { departamentoDAO.create(departamento); }
    public void actualizarDepartamento(Departamento departamento) { departamentoDAO.update(departamento); }
    public void eliminarDepartamento(int idDepartamento)        { departamentoDAO.delete(idDepartamento); }
}