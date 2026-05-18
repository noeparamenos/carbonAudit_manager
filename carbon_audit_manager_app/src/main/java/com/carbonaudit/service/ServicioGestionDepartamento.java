package com.carbonaudit.service;

import com.carbonaudit.dao.DepartamentoDAO;
import com.carbonaudit.model.Departamento;
import com.carbonaudit.util.Validador;

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

    public void crearDepartamento(Departamento departamento) {
        Validador.validarDireccion(departamento.getDireccion());
        departamentoDAO.create(departamento);
    }

    public void actualizarDepartamento(Departamento departamento) {
        Validador.validarDireccion(departamento.getDireccion());
        departamentoDAO.update(departamento);
    }
    public void eliminarDepartamento(int idDepartamento)        { departamentoDAO.delete(idDepartamento); }
}